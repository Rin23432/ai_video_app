Place on-device model files here:
- tag_suggest.tflite
- tag_suggest.labels.txt

For llama.cpp + GGUF:
- GGUF is not packaged into APK by default because file is too large.
- Put GGUF at runtime path:
  files/models/qwen2.5-3b-instruct-q4_k_m.gguf
- Current app routing for tag suggestion:
  llama.cpp (GGUF) -> TFLite -> local rule fallback

Current runtime expects:
- input[0]: FLOAT32 tensor, shape [1, N]
- output[0]: FLOAT32 tensor, shape [1, M]

Production mapping rule (recommended):
- model output index i maps to labels file line i
- app maps label name -> current UI tag candidates by normalized name

Compatibility mapping rule (fallback):
- if labels file missing or labels count != output M,
  app falls back to legacy "candidate index alignment" mode.

Notes:
- Keep `tag_suggest.labels.txt` in sync with model class order.
- Use UTF-8 encoding for labels file.
