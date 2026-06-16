package me.lxb.writedone.ui.screens.legal

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun UserAgreementBody() {
    LRule()
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("最后更新日期:") }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("生效日期:") }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        append("本协议是开发者 ")
        withStyle(boldSpan) { append("lxb") }
        append(" (以下简称\"我们\"或\"开发者\") 与您 (以下简称\"用户\"或\"您\") 之间就使用 ")
        withStyle(boldSpan) { append("\"WriteDone\"") }
        append(" 移动应用 (以下简称\"本应用\") 所订立的协议。请您仔细阅读本协议的全部条款, 一旦您开始使用本应用即视为您已阅读、理解并接受本协议的所有条款。")
    })

    LRule()

    LH2("一、关于本应用")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("WriteDone") }
        append(" 是一款离线个人时间管理工具, 灵感来自 Git 的\"原子提交\"理念, 鼓励用户在自由时间主动做点有意义的事并记录下来。")
    })
    LH3("1.1 核心功能")
    LBullet("用户写下想做的事情, 点击开始后进入计时")
    LBullet("计时完成后保存为一次\"提交\" (类似 git commit)")
    LBullet("提交后进入 5 分钟休息倒计时")
    LH3("1.2 应用特点")
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("无需注册账号") }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("不联网") }; append(" (无任何后端服务)")
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("不收集任何用户数据") }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("不包含广告") }
    })
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("不收取任何费用") }
    })
    LBullet("全部数据存储于用户设备本地 (Android 应用沙箱目录)")

    LRule()

    LH2("二、使用许可")
    LH3("2.1 授予许可")
    LPRich(buildAnnotatedString {
        append("在您遵守本协议的前提下, 我们授予您")
        withStyle(boldSpan) { append("个人、非独占、不可转让、不可转授权") }
        append("的使用本应用的权利, 仅供您个人非商业使用。")
    })
    LH3("2.2 禁止行为")
    LP("您不得从事以下行为:")
    LBullet("复制、修改、改编、翻译、反编译、反向工程、反汇编本应用 (除适用法律明确允许)")
    LBullet("将本应用用于任何商业目的")
    LBullet("基于本应用开发衍生产品")
    LBullet("移除、隐藏或修改本应用中的任何版权、商标或其他专有标记")
    LBullet("从事违反中华人民共和国法律法规的活动")
    LBullet("利用本应用从事任何侵害他人合法权益的行为")

    LRule()

    LH2("三、用户内容")
    LH3("3.1 所有权")
    LP("您在 WriteDone 中创建的所有内容 (包括但不限于\"提交\"记录、计时数据等, 以下简称\"用户内容\") 由您独立创作, 其知识产权归您所有。我们不主张对用户内容的任何所有权。")
    LH3("3.2 我们的访问")
    LPRich(buildAnnotatedString {
        append("由于本应用为")
        withStyle(boldSpan) { append("纯本地应用") }
        append(", 我们")
        withStyle(boldSpan) { append("没有任何后端服务器") }
        append(", ")
        withStyle(boldSpan) { append("我们无法访问、收集、查看或使用您的任何用户内容") }
        append("。您的数据仅存在于您的设备上, 处于您的完全控制之下。")
    })

    LRule()

    LH2("四、隐私保护")
    LP("本应用尊重并保护您的隐私。本应用的隐私实践详见《WriteDone 隐私政策》, 该政策构成本协议不可分割的一部分。")

    LRule()

    LH2("五、数据与免责")
    LH3("5.1 数据存储")
    LP("您的用户内容存储于您的设备本地, 存储期限为:")
    LBullet("数据保留在您的设备上, 直到您主动清除或卸载本应用")
    LBullet("卸载本应用后, 所有用户内容将被永久删除且无法恢复")
    LH3("5.2 免责声明")
    LPRich(buildAnnotatedString {
        append("本应用按\"")
        withStyle(boldSpan) { append("现状") }
        append("\"和\"")
        withStyle(boldSpan) { append("可提供") }
        append("\"的基础提供。在法律允许的最大范围内, 我们不对以下情况承担任何责任:")
    })
    LBullet("因设备故障、误删、刷机、系统重置、恢复出厂设置等导致数据丢失")
    LBullet("因 Android 系统版本、设备型号、硬件差异等导致的兼容性问题")
    LBullet("因您违反本协议或不当使用本应用而造成的任何损失")
    LBullet("因不可抗力 (包括但不限于自然灾害、战争、政策变化) 导致的服务中断或异常")
    LBullet("第三方对您设备的任何行为造成的损失")
    LH3("5.3 建议")
    LPRich(buildAnnotatedString {
        append("我们")
        withStyle(boldSpan) { append("强烈建议") }
        append("您定期通过系统备份等方式保护您的设备数据。对于重要的提交记录, 请您自行通过截屏等方式备份。")
    })

    LRule()

    LH2("六、知识产权")
    LH3("6.1 应用知识产权")
    LP("本应用 (包括但不限于源代码、UI 设计、文案、图标、Logo 等) 的所有知识产权归开发者 lxb 所有, 受中华人民共和国著作权法、专利法、商标法等法律法规保护。")
    LH3("6.2 第三方组件")
    LP("本应用使用 Flutter 开源框架及若干开源组件 (如 sqflite、path_provider、intl、shared_preferences 等), 这些组件遵循其各自的许可证, 详见项目仓库中的依赖说明。")
    LH3("6.3 反馈")
    LPRich(buildAnnotatedString {
        append("如您向我们提供反馈、建议或意见, 您授予我们")
        withStyle(boldSpan) { append("非独占、免费、永久、不可撤销") }
        append("的使用该等反馈的权利, 用于改进本应用或开发新产品。")
    })

    LRule()

    LH2("七、协议变更")
    LH3("7.1 变更权")
    LP("我们保留根据业务发展、法律法规变更、技术升级等情况随时修改本协议的权利。")
    LH3("7.2 通知方式")
    LP("协议变更后, 我们将通过以下方式之一通知您:")
    LBullet("在本应用启动时通过弹窗提示")
    LBullet("在本项目的 GitHub 仓库发布更新")
    LBullet("通过应用内\"关于\"页面更新")
    LH3("7.3 生效")
    LP("变更后的协议自公布之日起生效。继续使用本应用即视为您接受变更后的协议。如您不同意变更, 您应停止使用本应用并卸载之。")

    LRule()

    LH2("八、协议终止")
    LH3("8.1 您主动终止")
    LPRich(buildAnnotatedString {
        append("您可以通过")
        withStyle(boldSpan) { append("随时卸载本应用") }
        append("来终止本协议。卸载后, 您的本地数据将被清除, 本协议自动终止。")
    })
    LH3("8.2 我们终止")
    LP("如您严重违反本协议 (包括但不限于从事本协议第二、三条禁止的行为), 我们保留随时终止本协议、停止您使用本应用的权利。")

    LRule()

    LH2("九、适用法律与争议解决")
    LH3("9.1 适用法律")
    LPRich(buildAnnotatedString {
        append("本协议的订立、执行、解释及争议解决均适用")
        withStyle(boldSpan) { append("中华人民共和国法律") }
        append(" (为本协议之目的, 不包括香港特别行政区、澳门特别行政区和台湾地区的法律)。")
    })
    LH3("9.2 争议解决")
    LPRich(buildAnnotatedString {
        append("因本协议引起的或与本协议有关的任何争议, 双方应首先通过友好协商解决。协商不成的, 任何一方均可将争议提交至")
        withStyle(boldSpan) { append("被告所在地有管辖权的人民法院") }
        append("通过诉讼方式解决。")
    })

    LRule()

    LH2("十、可分割性")
    LP("本协议的任何条款被认定为无效、非法或不可执行的, 不影响其他条款的效力。在该等情况下, 其他条款仍应得到完整执行。")

    LRule()

    LH2("十一、联系我们")
    LP("如您对本协议有任何疑问、意见或建议, 请通过以下方式联系我们:")
    LContactLine(label = "邮箱", value = "lxb031018@163.com")
    LContactLine(label = "GitHub", value = "github.com/lxb031018/lishi")
    LContactLine(label = "项目主页", value = "github.com/lxb031018/lishi")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("我们将在收到您的反馈后 15 个工作日内予以回复。") }
    })

    LRule()

    LH2("十二、生效")
    LP("本协议自您首次启动本应用并点击\"同意并继续\"之时起生效。")

    LRule()

    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("© 2026 lxb. 保留所有权利。") }
    })
    LPRich(buildAnnotatedString {
        withStyle(italicSpan) { append("本协议参考《中华人民共和国民法典》《中华人民共和国消费者权益保护法》《中华人民共和国个人信息保护法》等相关法律法规起草。") }
    })
}

@Composable
fun PrivacyPolicyBody() {
    LRule()
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("最后更新日期:") }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("生效日期:") }
        append(" 2026-06-08")
    })
    LPRich(buildAnnotatedString {
        append("您的隐私对我们至关重要。本隐私政策说明 ")
        withStyle(boldSpan) { append("\"WriteDone\"") }
        append(" 移动应用 (以下简称\"本应用\") 如何处理您的信息。请您仔细阅读本政策, 一旦您开始使用本应用即视为您已阅读、理解并接受本政策的全部内容。")
    })
    LP("本政策依据《中华人民共和国个人信息保护法》《中华人民共和国网络安全法》《App 违法违规收集使用个人信息行为认定方法》等相关法律法规制定。")

    LRule()

    LH2("一、我们收集的信息")
    LH3("1.1 核心原则")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("本应用不收集任何个人信息") }
        append("。")
    })
    LP("\"个人信息\"是指以电子或者其他方式记录的与已识别或者可识别的自然人有关的各种信息, 不包括匿名化处理后的信息。")
    LH3("1.2 具体说明")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("本应用不收集以下任何信息:") }
    })
    LBullet("身份信息 (姓名、手机号、身份证号) — 不需要注册账号")
    LBullet("位置信息 (精确 / 粗略) — 不申请位置权限")
    LBullet("通讯录 / 短信 / 通话记录 — 不申请相关权限")
    LBullet("设备识别码 (IMEI / MAC / Android ID / OAID) — 不申请相关权限")
    LBullet("相机 / 麦克风 / 相册 — 不申请相关权限")
    LBullet("传感器数据 — 不主动读取")
    LBullet("行为日志 / 使用统计 — 未集成任何分析 SDK")
    LBullet("崩溃 / 错误信息 — 未集成任何崩溃 SDK")
    LBullet("网络请求 — 应用运行期间不联网")
    LH3("1.3 适用人群")
    LPRich(buildAnnotatedString {
        append("本应用适合")
        withStyle(boldSpan) { append("所有年龄段") }
        append("人群使用, 不会因用户年龄差别对待或限制功能。鉴于本应用不收集任何个人信息, 监护人同意等机制在本应用中没有适用场景。")
    })

    LRule()

    LH2("二、信息的存储")
    LH3("2.1 存储位置")
    LPRich(buildAnnotatedString {
        append("您在 WriteDone 中创建的所有用户内容 (如\"读了 30 页书\"、用时 25 分钟等提交记录) ")
        withStyle(boldSpan) { append("仅存储在您的设备本地") }
        append(", 具体位置为 Android 系统的应用沙箱目录 (即 /data/data/ 之类的私有目录)。")
    })
    LH3("2.2 存储期限")
    LP("不同情形下的数据保留与后果:")
    LBullet("正常使用 — 数据保留在设备本地, 长期保留")
    LBullet("您在应用内删除单条记录 — 该条数据立即从本地删除")
    LBullet("您在系统设置中清除应用数据 — 所有本地数据被清除")
    LBullet("您卸载本应用 — 所有本地数据被永久删除, 且无法恢复")
    LH3("2.3 我们的访问能力")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("我们没有任何后端服务器, 因此我们无法访问、查看、修改、删除或恢复您的任何本地数据") }
        append("。您的数据完全处于您自己的控制之下。")
    })

    LRule()

    LH2("三、信息的使用")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("由于本应用不收集任何信息") }
        append(", 因此不存在\"使用\"环节。")
    })
    LP("您在本地创建的数据, 仅为实现本应用的\"提交记录\"功能而被本应用读取和展示。")

    LRule()

    LH2("四、信息的共享、转让、公开披露")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("由于本应用不收集任何信息") }
        append(", 因此不存在\"共享、转让、公开披露\"环节。")
    })
    LP("我们承诺:")
    LBullet("不向任何第三方共享您的任何信息 (因为我们没有您的信息)")
    LBullet("不向任何第三方转让您的任何信息")
    LBullet("不公开披露您的任何信息")
    LBullet("不进行任何形式的用户画像、行为分析、广告推送")

    LRule()

    LH2("五、第三方 SDK 与服务")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("本应用未集成任何第三方 SDK") }
        append(", 包括但不限于:")
    })
    LBullet("统计 / 分析 SDK (如 Google Analytics、友盟、百度统计、Mixpanel)")
    LBullet("崩溃 / 错误上报 SDK (如 Firebase Crashlytics、Sentry、Bugly)")
    LBullet("广告 SDK (如 AdMob、穿山甲、优量汇)")
    LBullet("社交分享 SDK (如友盟分享、ShareSDK)")
    LBullet("推送 SDK (如极光推送、个推、Firebase Cloud Messaging)")
    LBullet("支付 SDK")
    LP("如果您将来看到本应用集成上述任何 SDK, 请立即通过本政策第九条所列方式与我们联系, 确认是否为官方版本。")

    LRule()

    LH2("六、申请的 Android 权限")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("本应用当前版本不申请任何 Android 权限") }
        append("。")
    })
    LP("未来版本中, 如需新增以下功能, 将申请对应权限 (届时本政策将更新, 并在新版本中向您说明):")
    LBulletRich(buildAnnotatedString {
        withStyle(boldSpan) { append("本地通知 (提醒开始)") }
        append(" — 计划申请 ")
        withStyle(codeSpan) { append("POST_NOTIFICATIONS") }
        append(", 用于在合适时间提醒您开始一件新的事; 您可在系统设置 → 应用 → WriteDone → 通知中随时关闭。")
    })
    LPRich(buildAnnotatedString {
        append("我们")
        withStyle(boldSpan) { append("不会") }
        append("申请以下权限 (永久):")
    })
    LBullet("定位权限 (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION)")
    LBullet("相机权限 (CAMERA)")
    LBullet("麦克风权限 (RECORD_AUDIO)")
    LBullet("通讯录权限 (READ_CONTACTS)")
    LBullet("短信权限 (READ_SMS / SEND_SMS)")
    LBullet("存储权限 (本应用数据存储于应用沙箱, 不需外部存储权限)")
    LBullet("电话权限 (READ_PHONE_STATE)")
    LBullet("后台运行权限 (除本地通知外)")

    LRule()

    LH2("七、用户权利")
    LPRich(buildAnnotatedString {
        append("根据《中华人民共和国个人信息保护法》及相关法律法规, 您对自己的数据享有以下权利:")
    })
    LH3("7.1 访问权")
    LP("您可以在应用内的\"提交记录\"页面随时查看您创建的所有数据。")
    LH3("7.2 删除权")
    LP("您可以通过以下方式删除您的数据:")
    LBullet("在应用内删除单条记录 (未来版本将提供)")
    LBullet("清除应用数据 (系统设置 → 应用 → WriteDone → 存储 → 清除数据)")
    LBullet("卸载应用 (永久删除所有数据)")
    LH3("7.3 导出权 (未来)")
    LP("未来版本可能提供\"导出数据\"功能, 让您将本地数据导出为通用格式 (如 JSON、CSV)。")
    LH3("7.4 撤回同意")
    LPRich(buildAnnotatedString {
        append("您可以随时通过卸载本应用撤回对本政策的同意, 撤回后我们将")
        withStyle(boldSpan) { append("不再处理您的信息") }
        append(" (因我们从未处理过)。")
    })
    LH3("7.5 投诉举报")
    LP("如您认为本应用违反法律法规或本政策约定收集使用您的个人信息, 您可以通过本政策第九条所列方式联系我们, 或向网信、市场监督管理部门投诉举报。")

    LRule()

    LH2("八、政策的变更")
    LH3("8.1 变更权")
    LP("我们可能根据以下情况修改本政策:")
    LBullet("业务功能调整")
    LBullet("法律法规变更")
    LBullet("技术升级")
    LBullet("用户反馈建议")
    LH3("8.2 变更方式")
    LP("任何变更将通过以下方式公布:")
    LBullet("本项目的 GitHub 仓库: github.com/lxb031018/lishi")
    LBullet("应用内\"关于\"页面 (如有)")
    LBullet("应用启动时的弹窗提示 (重大变更时)")
    LH3("8.3 生效")
    LP("变更后的政策自公布之日起生效, 您继续使用本应用即视为接受。如您不同意变更, 请停止使用并卸载本应用。")

    LRule()

    LH2("九、联系我们")
    LP("如您对本隐私政策有任何疑问、意见、建议或投诉, 请通过以下方式联系我们:")
    LContactLine(label = "邮箱", value = "lxb031018@163.com")
    LContactLine(label = "GitHub", value = "github.com/lxb031018/lishi/issues")
    LContactLine(label = "项目主页", value = "github.com/lxb031018/lishi")
    LPRich(buildAnnotatedString {
        append("为保障您的反馈得到有效处理, 请在邮件主题中注明 ")
        withStyle(boldSpan) { append("\"WriteDone + 反馈类型 (如: 隐私 / 协议 / Bug / 建议)\"") }
        append("。")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("我们将在收到您的反馈后 15 个工作日内予以回复。") }
    })

    LRule()

    LH2("十、法律适用与争议解决")
    LH3("10.1 适用法律")
    LPRich(buildAnnotatedString {
        append("本政策的订立、执行、解释及争议解决均适用")
        withStyle(boldSpan) { append("中华人民共和国法律") }
        append("。")
    })
    LH3("10.2 争议解决")
    LPRich(buildAnnotatedString {
        append("因本政策引起的或与本政策有关的任何争议, 双方应首先通过友好协商解决。协商不成的, 任何一方均可将争议提交至")
        withStyle(boldSpan) { append("被告所在地有管辖权的人民法院") }
        append("通过诉讼方式解决。")
    })

    LRule()

    LH2("十一、生效")
    LP("本政策自您首次启动本应用并点击\"同意并继续\"之时起生效。")

    LRule()

    LH2("附:关键术语解释")
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("个人信息:") }
        append(" 以电子或者其他方式记录的与已识别或者可识别的自然人有关的各种信息")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("敏感个人信息:") }
        append(" 一旦泄露或者非法使用, 容易导致自然人的人格尊严受到侵害或者人身、财产安全受到危害的个人信息, 包括生物识别、宗教信仰、特定身份、医疗健康、金融账户、行踪轨迹等信息, 以及不满十四周岁未成年人的个人信息")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("收集:") }
        append(" 通过主动填写、上传、自动采集等方式获取个人信息")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("共享:") }
        append(" 向其他个人信息处理者提供个人信息")
    })
    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("SDK:") }
        append(" Software Development Kit, 第三方软件开发工具包")
    })

    LRule()

    LPRich(buildAnnotatedString {
        withStyle(boldSpan) { append("© 2026 lxb. 保留所有权利。") }
    })
    LPRich(buildAnnotatedString {
        withStyle(italicSpan) { append("本政策依据《中华人民共和国个人信息保护法》《中华人民共和国网络安全法》《信息安全技术 个人信息安全规范》(GB/T 35273) 等相关法律法规及国家标准起草。") }
    })
}
