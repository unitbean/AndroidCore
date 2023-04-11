import java.io.File
import java.util.Properties
import java.io.InputStreamReader
import java.io.FileInputStream
import org.gradle.api.Project

object Common {
    fun Project.getLocalProperty(key: String, file: String = "gradle.properties"): String {
        val properties = Properties()
        val localProperties = File(this.rootDir, file)
        if (localProperties.isFile) {
            InputStreamReader(FileInputStream(localProperties), Charsets.UTF_8)
                .use { reader ->
                    properties.load(reader)
                }
        } else error("File $file not found")

        return properties.getProperty(key)
    }
}