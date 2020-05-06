# OrangeChannel

使用 **Android QQ** 与 **WeChat Web** 为 QQ 微信 实现消息的高性能框架

---

## 能做什么
- 使用 qq bot 管理来自 微信 的消息
- 支持图片与非原生表情包
- 使用命令行管理
- 支持快速回复与快速切换

## 需要的环境
> Java 8

## 快速开始
使用 `java -jar OrangeChannel-xxxx.jar` 启动服务

将会自动生成 `config.yml` 配置文件

```yaml
bot:
  # bot 账号
  account:
  # bot 密码
  password:
owner:
  # 主人账号
  account:
```

> 请注意 `account` 是 `Long` 类型, 而不是 `String` 请勿添加 `'` 或者 `"`

填写完成后回到控制台输入 `[t, true]` 同意服务条款, Bot 将会自动启动

> **请注意 Bot 与主人账号须为好友**

启动后可能会提示需要验证码, 在新设备登录一般都会触发, 请按提示输入

完成后收到 Bot 发来的微信登录二维码图片 代表登录初始化已经成功

扫描二维码登录微信即可

![登录示例](https://i.loli.net/2020/05/06/d3mao5lp9zRihr8.png)

## 如何使用

看到这里你应该已经成功登录 Bot 了, 现在我们要学习如何使用

此时我们已经可以看到来自微信的消息了

消息的格式看起来应该是这样的 `[群组] 微信名 「备注名」: 消息`

`[群组]` 和 `「备注名」` 都是可选的 也就是说在没有的时候不会显示

来自自己的消息则会显示为 `你的微信名 「自己」: 消息` 不会显示来自的群组

---
既然可以收消息 那必然可以发消息

默认情况下你对机器人说什么都是没有用的因为你还没有选择发消息的对象

你可以使用

```
#use [微信名|备注|群组名]
```
来选择当前使用的聊天 支持模糊匹配

如果选择的聊天并不是你所想选的请使用更多的关键词

这只是 `#use` 命令的最简单的模式, 全参看起来应该是这样的

```
#use [微信名|备注|群组名] [a / any, f / friend, g / group] [l, list]
```

第一个参数为查询的关键词 可以是 `微信名|备注|群组名` 可选 无默认值

第二个参数为搜索的类型 `a / any` 为任何 `f / friend` 为好友 `g / group` 为群组 默认为 `any` 任何

第三个参数是可选的 只可为 `l / list` 代表显示搜索结果列表 并将搜索列表存入 `list` 以便下次操作(详情请查看命令`#list`), 默认为 `无` 代表直接选择不存入列表

当然 你也可以不加任何参数使用 `#use` 意为查询当前选择的聊天

> 示例
```
#use 海豚 f l
```
将会列出 `微信名|备注` 里有 `海豚` 的好友 并存入 `list`

---

可以选择聊天, 那肯定也可以取消选择, 使用
```
#unuse
```

就可以取消选择当前的聊天了, 没有任何参数, 就是这么简单

---

至此我们讲完了 `#use` 和 `#unuse` 命令, 不过单单会选择聊天可是远远不够的

如果我们遇到两个聊天时, 切换将会变得繁琐和措手不及, 此时就要用到一个命令了

```
#swap
```

意为交换, 可以将当前 `use` 的聊天与 `swap` 区的聊天进行交换, 没有任何参数

也就是你可以选择一个聊天后使用 `#swap` 将聊天切入后台 再选择另一个聊天 并使用 `#swap` 命令在两个聊天之前切换

当然也可以作为防误触, 将聊天切入后台, 使用时再次切出

---

现在你已经学会了如何在两个聊天之间切换了, 可是如果同时有更多的聊天怎么办

切换起来岂不是更加的繁琐混乱, 这就要用到另外的技巧了 `快速回复` 和 `快速选择`

这时我们就要用到一个 qq 的原生功能了

![回复.png](https://i.loli.net/2020/05/06/B5uNREDFaYzfjs7.png)

> 快速回复

你可以对一个 Bot 发送的来自微信的消息进行回复, 回复的消息就会自动发回给发送方了

注意哦, 如果回复的消息来自群组, 消息会自动发回给群组, 而不是个人, 以及回复自己发送的消息是没有任何用的

> 快速选择

把刚刚回复的内容改为 `use` 就可以了, 不过还是要注意哦没有 `#` ,使用后当前聊天将会被切换到回复的发送方

---

基础操作都讲完了, 后面就是进阶操作了
```
#list [操作] [参数]...
```
此命令用于操作 `list`, 就是之前使用 `#use` 所保存的列表, 现在操作仅支持 `use` 选择

操作可选, 不填将显示当前 `list` 中的内容

> 显示示例

```
0 | [用户] 微信名 「备注」
2 | [用户] 微信名 「备注」
3 | [用户] 微信名 「备注」
4 | [群组] 群组名
5 | [群组] 群组名
6 | [群组] 群组名
```

> 请注意索引是从 `0` 开始, 而不是 `1`

操作现仅支持 `use` 选择列表中的用户

> 示例

```
#list use 0
```

将会把聊天切换到列表中索引为 `0` 的聊天

> 此功能待开发, 以后会加入更多操作, 感谢支持!

---

> 容错

所有命令的 `#` 前缀均可以换成 `£`

---

## 贡献

欢迎任何人以任何形式做出任何贡献

也希望有更多人参与开发

---

## FAQ

> Q: 为何微信有时会无缘无故掉线

A: 腾讯的规则为网页微信必须在手机微信登录时才可以登录, 长时间不登录微信将会被判定为掉线, 故网页端也会退出, 现网页端登录退出后已会再次尝试发送二维码, 仅需再次扫描二维码登录即可

> Q: 这个项目是怎么来的

A: 觉得微信太差劲~~(Telegram NB)~~, 又因为某些 `不可抗力` 因素导致不得不使用微信, 故将微信消息转发至 qq

> Q: 为什么叫 OrangeChannel

A: 这个...? 不知道取啥名随便想的, `橘子信道` 不是挺好听的嘛

> Q: 我觉得这个命令用起来不顺手, 可以修改么?

A: 当然可以改, 但是这需要你行修改自行编译

> Q: 为什么容错字符是个 `£` 而不是 `XXX`?

A: 容错机制本身就是因为我使用的 win10 语言切换, 习惯写代码 or 纯英文环境时切到英国键盘, 防止误触 英文键盘 `#` 键的对应输出就是 `£` , 如果您想更改, 请参考上一个问题

> Q: 为什么版本是 `1.1.0` , `1.0.0` 去哪了?

A: ~~被我吃了~~ 其实基础版本已经写好一段时间了, 只不过太懒了就鸽了

## 许可证

本项目的许可证为 **`GNU AFFERO GENERAL PUBLIC LICENSE version 3`**

## 使用的库

**[chatapi-wechat](https://github.com/xuxiaoxiao-xxx/ChatApi-WeChat)** `微信 Web 端`

**[mirai](https://github.com/mamoe/mirai)** `手机 QQ 端`

**[guava](https://github.com/google/guava)** `Cache 缓存`

**[konfig-yaml](https://github.com/mamoe/konfig)** `Yaml`

**[kotlin](https://github.com/JetBrains/kotlin)** `Kotlin`

**[ktor-http-cio](https://github.com/ktorio/ktor/tree/master/ktor-http/ktor-http-cio)** `Http Client`