package foundry.veil.kotlinscript

import de.swirtz.ktsrunner.objectloader.KtsObjectLoader
import foundry.veil.Veil
import foundry.veil.platform.Services
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.name


// TODO: Figure out a way to have the dsl read as a single object
class RenderTypeCompiler {
    var RENDER_TYPES: MutableMap<String, RenderType> = mutableMapOf()
    val RENDERTYPE_FOLDER: Path = Services.PLATFORM.gameDir.resolve("rendertypes")

    companion object {
        val INSTANCE = RenderTypeCompiler()
        fun init() {
            Veil.LOGGER.info("Loading render type scripts from path ${INSTANCE.RENDERTYPE_FOLDER}")
            // if folder doesnt exist, create it
            if (!INSTANCE.RENDERTYPE_FOLDER.toFile().exists()) {
                Veil.LOGGER.info("Render type folder does not exist, creating it")
                Files.createDirectory(INSTANCE.RENDERTYPE_FOLDER)
            }
            // Create a txt file in the folder containing the names of files in it
            val file = INSTANCE.RENDERTYPE_FOLDER.resolve("render_types.txt").toFile()
            if (!file.exists()) {
                Veil.LOGGER.info("Render type list file does not exist, creating it")
                file.createNewFile()
            } else {
                Veil.LOGGER.info("Render type list file exists, clearing it")
                file.writeText("")
            }
            // Write the names of the files in the folder to the txt file
            INSTANCE.getRenderTypeScripts().forEach {
                file.appendText(it.fileName.name + "\n")
            }


        }

        fun instance(): RenderTypeCompiler {
            return INSTANCE
        }
    }

    fun getRenderTypeScripts(): List<Path> {
        return RENDERTYPE_FOLDER.toFile().listFiles()?.filter { it.extension == "kts" }?.map { it.toPath() } ?: emptyList()
    }


    init {
        getRenderTypeScripts().forEach {
            Veil.LOGGER.info("Found render type script: $it")
            RENDER_TYPES[it.fileName.name] = KtsObjectLoader().load(Files.newBufferedReader(it))
        }
        println(RENDER_TYPES)
    }
}