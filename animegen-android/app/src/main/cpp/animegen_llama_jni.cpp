#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>

#define LOG_TAG "animegen_llama_jni"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)

#ifdef ANIMEGEN_ENABLE_LLAMA_CPP
#include "llama.h"
#endif

static std::string jstring_to_std(JNIEnv* env, jstring input) {
    if (input == nullptr) return "";
    const char* chars = env->GetStringUTFChars(input, nullptr);
    if (chars == nullptr) return "";
    std::string result(chars);
    env->ReleaseStringUTFChars(input, chars);
    return result;
}

static jstring make_json_error(JNIEnv* env) {
    static const char* kFallback = "{\"tags\":[]}";
    return env->NewStringUTF(kFallback);
}

#ifdef ANIMEGEN_ENABLE_LLAMA_CPP
static jstring infer_with_llama(
    JNIEnv* env,
    const std::string& model_path,
    const std::string& prompt,
    jint max_tokens,
    jfloat temperature,
    jfloat top_p,
    jint seed,
    jint threads,
    jint context_size
) {
    llama_backend_init();

    llama_model_params model_params = llama_model_default_params();
    model_params.n_gpu_layers = 0;

    llama_model* model = llama_model_load_from_file(model_path.c_str(), model_params);
    if (model == nullptr) {
        LOGW("llama model load failed: %s", model_path.c_str());
        return make_json_error(env);
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = context_size;
    ctx_params.n_threads = threads;
    ctx_params.n_threads_batch = threads;

    llama_context* ctx = llama_init_from_model(model, ctx_params);
    if (ctx == nullptr) {
        LOGW("llama context init failed");
        llama_model_free(model);
        return make_json_error(env);
    }

    const llama_vocab* vocab = llama_model_get_vocab(model);
    const bool add_special = true;
    const bool parse_special = true;
    const int32_t max_prompt_tokens = context_size > 32 ? context_size - 32 : context_size;
    std::vector<llama_token> tokens(max_prompt_tokens);
    const int n_prompt = llama_tokenize(
        vocab,
        prompt.c_str(),
        static_cast<int32_t>(prompt.size()),
        tokens.data(),
        static_cast<int32_t>(tokens.size()),
        add_special,
        parse_special
    );

    if (n_prompt <= 0) {
        LOGW("llama tokenize failed");
        llama_free(ctx);
        llama_model_free(model);
        return make_json_error(env);
    }
    tokens.resize(n_prompt);

    llama_batch batch = llama_batch_get_one(tokens.data(), static_cast<int32_t>(tokens.size()));
    if (llama_decode(ctx, batch) != 0) {
        LOGW("llama decode prompt failed");
        llama_free(ctx);
        llama_model_free(model);
        return make_json_error(env);
    }

    std::string output_text;
    output_text.reserve(static_cast<size_t>(max_tokens) * 8);

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(sampler, llama_sampler_init_top_p(top_p, 1));
    llama_sampler_chain_add(sampler, llama_sampler_init_temp(temperature));
    llama_sampler_chain_add(sampler, llama_sampler_init_dist(seed));

    for (int i = 0; i < max_tokens; ++i) {
        llama_token next = llama_sampler_sample(sampler, ctx, -1);
        if (llama_token_is_eog(vocab, next)) {
            break;
        }

        char piece[256];
        const int n = llama_token_to_piece(vocab, next, piece, sizeof(piece), 0, true);
        if (n > 0) {
            output_text.append(piece, static_cast<size_t>(n));
        }

        llama_batch next_batch = llama_batch_get_one(&next, 1);
        if (llama_decode(ctx, next_batch) != 0) {
            LOGW("llama decode token failed");
            break;
        }
    }

    llama_sampler_free(sampler);
    llama_free(ctx);
    llama_model_free(model);

    if (output_text.empty()) {
        return make_json_error(env);
    }
    return env->NewStringUTF(output_text.c_str());
}
#endif

extern "C"
JNIEXPORT jstring JNICALL
Java_com_animegen_app_ai_LlamaCppNativeBridge_infer(
    JNIEnv* env,
    jobject /* thiz */,
    jstring modelPath,
    jstring prompt,
    jint maxTokens,
    jfloat temperature,
    jfloat topP,
    jint seed,
    jint threads,
    jint contextSize
) {
    const std::string model_path = jstring_to_std(env, modelPath);
    const std::string prompt_text = jstring_to_std(env, prompt);

    if (model_path.empty() || prompt_text.empty()) {
        return make_json_error(env);
    }

#ifdef ANIMEGEN_ENABLE_LLAMA_CPP
    return infer_with_llama(
        env,
        model_path,
        prompt_text,
        maxTokens,
        temperature,
        topP,
        seed,
        threads,
        contextSize
    );
#else
    LOGI("llama.cpp disabled at build time, returning stub response");
    return make_json_error(env);
#endif
}
