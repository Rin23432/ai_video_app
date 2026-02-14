---
name: ai-inference-adapters
description: Use this skill for AI inference adapter interfaces and provider implementations for cloud models and optional on-device paths.
---

# AI Inference Adapters Skill

## Goal

在 `animegen-ai` 中实现“可插拔 AI 推理适配层”，对外提供统一接口 `AiProvider`，对内支持多家模型/服务（Mock、OpenAI、Gemini、本地推理、第三方平台等），并确保：

- 调用参数结构稳定
- 返回结果结构稳定
- 错误可观测、可重试、可降级
- 不污染业务层（service/worker/api 不关心具体供应商）

## Use when

- 需要新增/替换 AI 供应商（如从 Mock 改成 MinIO + 真模型）
- 需要支持多种能力：文生视频、图生视频、分镜生成、封面生成
- 需要对调用进行限流、超时、重试、熔断、降级
- 需要对提示词/参数做统一模板化与版本控制

## Inputs

- `AiRequest`（taskId/workId/prompt 等）
- 配置：provider 类型、baseUrl、apiKey、timeout、retry、rateLimit
- 可选：modelName、seed、duration、stylePreset、negativePrompt、assets

## Outputs

- `AiResult`：`videoUrl` / `coverUrl`（MVP）
- 可扩展：`metadata`（model、timing、cost、traceId）

## Interfaces

- `AiProvider#generate(AiRequest): AiResult`
- `AiProvider` 实现必须是无状态/线程安全（或自行保证）

## Conventions

- 业务层只依赖 `AiProvider` 接口，不依赖具体实现类
- Provider 选择通过 Spring Bean + 配置开关实现（推荐 `@ConditionalOnProperty`）
- 错误统一包装为 `AiException`（含 errorCode、retryable、rootCause）
- 超时必须有（避免 worker 堵死）
- 所有请求/响应（含耗时、状态码）都要 log，但敏感信息（key/token）必须脱敏

## Workflow

1. 定义/扩展 `AiRequest` 与 `AiResult`（保持向后兼容）
2. 新增 provider 实现（如 `OpenAiProvider` / `LocalProvider`）
3. 加入统一的 HTTP 客户端配置（超时、重试、连接池）
4. 在 worker 中保持“只调用 AiProvider”，不写供应商逻辑
5. 增加集成测试：mock server / wiremock（可选）

## Checklist (Definition of Done)

- [ ] 新 provider 可通过配置启用/禁用
- [ ] 失败场景能区分：可重试 vs 不可重试
- [ ] 超时/重试参数可配置
- [ ] 日志含 traceId/taskId/workId
- [ ] 不泄漏密钥/隐私信息

## Snippets

### AiException

```java
public class AiException extends RuntimeException {
  private final String errorCode;
  private final boolean retryable;
  public AiException(String code, boolean retryable, String msg, Throwable cause) {
    super(msg, cause);
    this.errorCode = code;
    this.retryable = retryable;
  }
  public String getErrorCode(){ return errorCode; }
  public boolean isRetryable(){ return retryable; }
}
```
