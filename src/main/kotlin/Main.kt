import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata
import org.apache.commons.imaging.formats.tiff.constants.MicrosoftTagConstants
import java.io.BufferedReader
import java.io.FileFilter
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.exists
import kotlin.streams.toList

fun main(args: Array<String>) {
    val searchFile:String = "__search.txt"
    var searchTag: List<String> = listOf()
    val path: String = ""
    val searchedDirectory: Path = Paths.get(path).toAbsolutePath().resolve("searched")
    var a: List<String>
    println(searchedDirectory)
    if (searchedDirectory.toFile().exists().not()) {
        Files.createDirectory(searchedDirectory)
    }
    if (!Files.exists(searchedDirectory.resolve(searchFile))) {
        Files.createFile(searchedDirectory.resolve(searchFile))
    }
    while (true) {
        a = tag(searchedDirectory.resolve(searchFile))
            ?: listOf("Empty")
        if (searchTag == a) {
            Thread.sleep(5000L)
            continue
        }
        searchTag = a
        println(a)
        searchedDirectory.toFile().listFiles { f ->
            f.isFile.and(
                f.toString().uppercase().endsWith(".JPG")
            )
        }
            ?.forEach { f4 -> f4.delete() }

        Paths.get(path).toAbsolutePath().toFile()
            .listFiles { pathname -> pathname.isFile.not() }
            ?.forEach { file ->
                file.listFiles { pathname ->
                    pathname.isFile.and(
                        pathname.toString().uppercase().endsWith(".JPG")
                    )
                }
                    ?.filter { f2 ->
//                        println(f2)
                        (Imaging.getMetadata(f2)
                            .takeIf { imageMetadata -> imageMetadata is JpegImageMetadata }
                            ?.let { imageMetadata -> imageMetadata as JpegImageMetadata }
                            ?.findEXIFValueWithExactMatch(MicrosoftTagConstants.EXIF_TAG_XPKEYWORDS)?.value as? String)?.split(
                            Regex("[; ,]")
                        )
                            ?.containsAll(a) == true
                    }?.forEach { f3 ->
                        Files.copy(
                            f3.toPath(),
                            searchedDirectory.resolve(f3.name),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                    }
            }
    }
}

fun tag(path:Path): List<String> {
    val newInputStream = Files.newInputStream(path, StandardOpenOption.READ)
    val reader = InputStreamReader(newInputStream,StandardCharsets.UTF_8)
    return BufferedReader(reader).lines().toList().firstOrNull()?.split(Regex("[; ,]"))?: listOf("Empty")
}
