# 视频商城与当前仓库模块映射

- `animegen-api`：新增 `Mall*Controller`，对外提供商品、订单、支付、权益、播放鉴权接口。
- `animegen-service`：新增 `Mall*Service`，承接下单、扣减库存、关单、发放权益业务编排。
- `animegen-dao`：新增 `video_*` 表对应 DO/Mapper/SQL，继续沿用 MyBatis 风格。
- `animegen-common`：补充商城错误码、订单状态枚举、统一返回 DTO。
- `animegen-worker`：消费支付成功和超时关单事件，执行异步权益发放与库存回补。

## 说明

该文件用于把 `docs/video-mall-module-design.md` 的架构设计映射到仓库实际模块，便于按模块分工实现。
