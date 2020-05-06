package pl.edu.moe.orangechannel

import java.io.File
import java.io.FileInputStream

/**
 * 获取文件的扩展名，图片类型的文件，会根据文件内容自动判断文件扩展名
 *
 * @param file 要获取文件扩展名的文件
 * @return 文件扩展名
 */
fun fileSuffix(file: File): String {
    FileInputStream(file).use { `is` ->
        val b = ByteArray(3)
        `is`.read(b, 0, b.size)
        return when (val fileCode = bytesToHex(b)) {
            "ffd8ff" -> "jpg"
            "89504e" -> "png"
            "474946" -> "gif"
            else -> when {
                fileCode.startsWith("424d") -> {
                    "bmp"
                }
                file.name.lastIndexOf('.') > 0 -> {
                    file.name.substring(file.name.lastIndexOf('.') + 1)
                }
                else -> {
                    ""
                }
            }
        }
    }
}

private val HEX = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
/**
 * 将字节数组转换成16进制字符串
 *
 * @param bytes 要转换的字节数组
 * @return 转换后的字符串，全小写字母
 */
private fun bytesToHex(bytes: ByteArray): String {
    val chars = CharArray(bytes.size * 2)
    for (i in bytes.indices) {
        val b = bytes[i]
        chars[i shl 1] = HEX[b.toInt() ushr 4 and 0xf]
        chars[(i shl 1) + 1] = HEX[b.toInt() and 0xf]
    }
    return String(chars)
}
