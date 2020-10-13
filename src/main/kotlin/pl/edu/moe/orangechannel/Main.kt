package pl.edu.moe.orangechannel

import com.google.common.cache.CacheBuilder
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.parse
import me.xuxiaoxiao.chatapi.wechat.WeChatClient
import me.xuxiaoxiao.chatapi.wechat.entity.contact.WXContact
import me.xuxiaoxiao.chatapi.wechat.entity.contact.WXGroup
import me.xuxiaoxiao.chatapi.wechat.entity.contact.WXUser
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXImage
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXMessage
import me.xuxiaoxiao.chatapi.wechat.entity.message.WXText
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.event.subscribeAlways
import net.mamoe.mirai.join
import net.mamoe.mirai.message.FriendMessageEvent
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.sendImage
import net.mamoe.mirai.message.sourceId
import net.mamoe.mirai.message.uploadImage
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.io.InputStream
import java.util.*
import kotlin.collections.HashSet
import kotlin.system.exitProcess

@OptIn(KtorExperimentalAPI::class)
val httpClient = HttpClient(CIO)
val listener = object : WeChatClient.WeChatListener()
{
    override fun onQRCode(client: WeChatClient, qrCode: String) {
        bot.launch {
            val `in` = httpClient.get<InputStream>(qrCode)
            val file = File("weChatQrCode.jpg")
            val out = file.outputStream()
            `in`.use {
                it.copyTo(out)
                out.close()
            }
            owner.sendImage(file)
            bot.launch {
                val unit = withTimeoutOrNull(5000)
                {
                    do System.gc() while (!file.delete())
                }
                if (unit == null) println("文件 $file 删除超时失败")
            }
        }
    }

    override fun onLogin(client: WeChatClient) {
        println("登录: 检测到 ${client.userFriends().size} 个好友, ${client.userGroups().size} 个群")
        bot.launch {
            owner.sendMessage(PlainText("登录成功"))
        }
    }

    override fun onMessage(client: WeChatClient, message: WXMessage) {
        var user = message.fromUser
        val group = message.fromGroup
        val isGroup = group != null

        if (user != null)
        {
            val me = client.userMe()
            var userRemark = user.remark
            val mySelf = user.id == me.id
            if (mySelf)
            {
                user = me
                userRemark = "自己"
            }
            when (message)
            {
                is WXText ->
                {
                    bot.launch {
                        val text = "${if (isGroup) "[${group.name}] " else ""}${user.name}${if (!userRemark.isNullOrBlank()) " 「$userRemark」" else ""}: ${message.content}"
                        val msg = owner.sendMessage(PlainText(text.replace("<br/>", "\n")))
                        if (isGroup) useCache.put(msg.sourceId, if (isGroup) group else user)
                    }
                }
                is WXImage ->
                {
                    bot.launch {
                        val wxImage = client.fetchImage(message)
                        val origin = wxImage.origin
                        val messageChain = buildMessageChain {
                            add("${if (isGroup) "[${group.name}] " else ""}${user.name}${if (!userRemark.isNullOrBlank()) " 「$userRemark」" else ""}: ")
                            add(owner.uploadImage(origin))
                        }
                        val msg = owner.sendMessage(messageChain)
                        if (isGroup) useCache.put(msg.sourceId, if (isGroup) group else user)
                        wxImage.image.delete()
                        bot.launch {
                            val unit = withTimeoutOrNull(5000)
                            {
                                do System.gc() while (!origin.delete())
                            }
                            if (unit == null) println("文件 $origin 删除超时失败")
                        }
                    }
                }
            }
        }
    }

    override fun onLogout(c: WeChatClient) {
        bot.launch {
            owner.sendMessage(PlainText("微信 Web 客户端正常退出, 请注意重登"))
        }
        use = null
        useCache.cleanUp()
        list.clear()
        client = WeChatClient()
        client.setListener(this)
        client.startup()
    }

    override fun onFailure(c: WeChatClient, reason: String) {
        bot.launch {
            owner.sendMessage(PlainText("微信 Web 客户端异常退出, 请注意重登"))
        }
        use = null
        useCache.cleanUp()
        list.clear()
        client = WeChatClient()
        client.setListener(this)
        client.startup()
    }
}

private lateinit var client: WeChatClient
private lateinit var bot: Bot
private lateinit var owner: Friend
private var use: WXContact? = null
private var swap: WXContact? = null
private var list: MutableList<WXContact> = mutableListOf()
private val useCache = CacheBuilder.newBuilder().maximumSize(1024).build<Int, WXContact>()

@OptIn(ExperimentalStdlibApi::class)
suspend fun main() {
    val configFile = File("config.yml")
    if (!configFile.exists())
    {
        Config::class.java.getResourceAsStream("/config.yml").use { `in` ->
            val out = configFile.outputStream()
            `in`.copyTo(out)
            out.close()
        }
        println("已成功生成配置文件, 请填写后同意服务条款后继续 [true/t, false/f]")
        val scanner = Scanner(System.`in`)
        while (true)
        {
            val string = scanner.nextLine()
            if (string.equals("true", true) || string.equals("t", true))
            {
                println("已同意服务条款, 开始加载")
                break
            }
            if (string.equals("false", true) || string.equals("f", true))
            {
                configFile.delete()
                println("您拒绝了服务条款, 结束运行")
                exitProcess(0)
            }
            println("参数错误, 必须为 [t / true, f / false]")
        }
    }
    val config = Yaml.default.decodeFromString(Config.serializer(), configFile.readText())
    bot = Bot(config.bot.account, config.bot.password).alsoLogin()
    owner = bot.getFriend(config.owner.account)
    bot.subscribeAlways<FriendMessageEvent> {
        if (it.sender != owner) return@subscribeAlways
        val content = it.message.contentToString()
        if (content.startsWith("#") || content.startsWith("£"))
        {
            val args = content.substring(1).split(" ").toMutableList()
            if (args.isNotEmpty())
            {
                when (args.removeFirst())
                {
                    "ping" ->
                    {
                        reply("pong")
                    }
                    "use" ->
                    {
                        if (args.isNotEmpty())
                        {
                            val search = args.removeFirst()

                            var friendSearch = true
                            var groupSearch = true
                            var showList = false
                            if (args.isNotEmpty())
                            {
                                val type = args.removeFirst()
                                if (type == "f" || type == "friend") groupSearch = false
                                else if (type == "g" || type == "group") friendSearch = false
                                else if (type != "a" && type != "any")
                                {
                                    reply("参数 2 错误 必须为 [f / friend, g / group, a / any] 或忽略 默认 [any]")
                                    return@subscribeAlways
                                }
                                if (args.isNotEmpty())
                                {
                                    val option = args.removeFirst()
                                    if (option == "l" || option == "list") showList = true
                                    else
                                    {
                                        reply("参数 3 错误 必须为 [l / list] 默认 无")
                                        return@subscribeAlways
                                    }
                                }
                            }

                            val friends = client.userFriends().values
                            val result : MutableSet<WXContact> = HashSet()
                            if (friendSearch)
                            {
                                result.addAll(friends.filter { wxUser -> wxUser.remark.contains(search) })
                                if (showList || result.isEmpty())
                                {
                                    result.addAll(friends.filter { wxUser -> wxUser.name.contains(search) })
                                }
                            }
                            if (groupSearch)
                            {
                                if (showList || result.isEmpty())
                                {
                                    result.addAll(client.userGroups().values.filter { wxGroup -> wxGroup.name.contains(search) })
                                }
                            }
                            if (result.isNotEmpty())
                            {
                                if (!showList) {
                                    val wxContact = result.first()
                                    use = wxContact
                                    reply(changeUse(wxContact))
                                }
                                else
                                {
                                    list = result.toMutableList()
                                    reply(getListMessageChain())
                                }
                            }
                            else
                            {
                                reply("未找到目标")
                            }
                        } else reply(showUse())
                    }
                    "list" ->
                    {
                        if (list.isEmpty())
                        {
                            reply("目前待操作列表为空")
                            return@subscribeAlways
                        }
                        if (args.isNotEmpty())
                        {
                            when (args.removeFirst())
                            {
                                "use" ->
                                {
                                    if (args.isNotEmpty())
                                    {
                                        try
                                        {
                                            val index = args.removeFirst().toInt()
                                            if (index >= 0 && index <= list.lastIndex)
                                            {
                                                val wxContact = list[index]
                                                use = wxContact
                                                reply(changeUse(wxContact))
                                            }
                                            else reply("参数 2 错误 索引越界")
                                        }
                                        catch (e: NumberFormatException)
                                        {
                                            reply("参数 2 错误 必须为索引")
                                        }
                                    } else reply("参数 2 缺失 必须为索引")
                                } else -> reply("参数 1 错误 必须为 [use] 必填")
                            }
                        } else reply(getListMessageChain())
                    }
                    "swap" ->
                    {
                        val temp = swap
                        swap =
                            use
                        use = temp
                        reply(showUse())
                    }
                    "unuse" ->
                    {
                        use = null
                        reply("已取消选择")
                    }
                    else -> reply("命令不存在")
                }
            }
        }
        else
        {
            val quoteReply = it.message.firstIsInstanceOrNull<QuoteReply>()
            if (quoteReply != null)
            {
                val wxContact = useCache.getIfPresent(quoteReply.source.id)
                if (wxContact != null)
                {
                    if (content == "use")
                    {
                        use = wxContact
                        reply(changeUse(wxContact))
                    }
                    else client.sendMessageChain(wxContact, it.message)
                }
            }
            else
            {
                val use = use
                if (use != null)
                {
                    client.sendMessageChain(use, it.message)
                }
            }
        }
    }
    client = WeChatClient()
    client.setListener(listener)
    client.startup()
    bot.join()
}

fun WeChatClient.sendMessageChain(wxContact: WXContact, messageChain: MessageChain)
{
    bot.launch {
        for (message in messageChain)
        {
            when (message)
            {
                is PlainText ->
                {
                    sendText(wxContact, message.content)
                }
                is Image ->
                {
                    val `in` = httpClient.get<InputStream>(message.queryUrl())
                    var file = File("${UUID.randomUUID()}-${System.currentTimeMillis()}")
                    val out = file.outputStream()
                    `in`.use {
                        `in`.copyTo(out)
                        out.close()
                    }

                    val suffix = fileSuffix(file)
                    if (suffix.isNotBlank())
                    {
                        val f = File("${file.name}.$suffix")
                        if (file.renameTo(f)) file = f
                    }

                    val wxImage = client.sendFile(wxContact, file) as WXImage
                    wxImage.image.delete()
                    file.delete()
                }
            }
        }
    }
}

fun getListMessageChain() = buildMessageChain {
    val messages = list.mapIndexed { index, wxContact ->
        when (wxContact) {
            is WXUser -> {
                val userRemark = wxContact.remark
                "$index | [用户] ${wxContact.name}${if (!userRemark.isNullOrBlank()) " 「$userRemark」" else ""}"
            }
            is WXGroup -> "$index | [群组] ${wxContact.name}"
            else -> ""
        }
    }.filter { s -> s.isNotBlank() }.map { s -> "$s\n" }.toMutableList()
    val lastIndex = messages.lastIndex
    messages[lastIndex] = messages[lastIndex].substringBeforeLast('\n')
    messages.forEach(this::add)
}

fun changeUse(wxContact: WXContact) = when (wxContact) {
    is WXUser -> {
        val userRemark = wxContact.remark
        use = wxContact
        "选择已更新为: [用户] ${wxContact.name}${if (!userRemark.isNullOrBlank()) " 「$userRemark」" else ""}"
    }
    is WXGroup -> {
        use = wxContact
        "选择已更新为: [群组] ${wxContact.name}"
    }
    else -> throw UnsupportedOperationException()
}

fun showUse(): String
{
    val use = use
    if (use == null) return "当前没有选择目标"
    else
    {
        return when (use)
        {
            is WXUser ->
            {
                val userRemark = use.remark
                "当前选择为: [用户] ${use.name}${if (!userRemark.isNullOrBlank()) " 「$userRemark」" else ""}"
            }
            is WXGroup ->
            {
                "当前选择为: [群组] ${use.name}"
            }
            else -> throw UnsupportedOperationException()
        }
    }
}
