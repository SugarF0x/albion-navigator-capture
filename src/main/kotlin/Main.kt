import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent
import com.github.kwhat.jnativehook.mouse.NativeMouseListener
import java.awt.MouseInfo
import java.awt.Rectangle
import java.awt.Robot
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.awt.image.MultiResolutionImage
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

class GlobalHotkeyListener : NativeKeyListener, NativeMouseListener {
    private var isCtrlPressed = false

    override fun nativeKeyPressed(e: NativeKeyEvent) {
        if (e.keyCode == NativeKeyEvent.VC_CONTROL) isCtrlPressed = true
    }

    override fun nativeKeyReleased(e: NativeKeyEvent) {
        if (e.keyCode == NativeKeyEvent.VC_CONTROL) isCtrlPressed = false
    }

    override fun nativeMousePressed(e: NativeMouseEvent) {
        if (isCtrlPressed && e.button == NativeMouseEvent.BUTTON1) takeScreenshot()
    }

    private fun takeScreenshot() {
        try {
            val robot = Robot()

            val screenRectangle = Rectangle(Toolkit.getDefaultToolkit().screenSize)
            val multiResolutionImage: MultiResolutionImage = robot.createMultiResolutionScreenCapture(screenRectangle)

            val screenshot = multiResolutionImage.resolutionVariants.maxByOrNull { img ->
                img.getWidth(null) * img.getHeight(null)
            }

            if (screenshot == null) {
                println("Failed to capture screenshot.")
                return
            }

            val tempDir = System.getProperty("java.io.tmpdir")
            val subDir = "albion-navigator"
            val fileName = "${System.currentTimeMillis()}.png"
            val filePath: Path = Paths.get(tempDir, subDir, fileName)

            Files.createDirectories(filePath.parent)

            val outputFile = File(filePath.toUri())
            ImageIO.write(screenshot as BufferedImage, "png", outputFile)

            println("Screenshot saved as ${outputFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun main() {
    try {
        System.setProperty("org.jnativehook.lib.logging.level", "OFF")

        GlobalScreen.registerNativeHook()
        val listener = GlobalHotkeyListener()
        GlobalScreen.addNativeKeyListener(listener)
        GlobalScreen.addNativeMouseListener(listener)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
