package io.github.gmazzo.android.manifest.lock

import com.charleskorn.kaml.encodeToStream
import io.github.gmazzo.android.manifest.lock.ManifestLockFactory.yaml
import org.gradle.api.artifacts.transform.CacheableTransform
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import java.util.TreeMap
import java.util.TreeSet
import java.util.zip.ZipFile
import kotlin.streams.asSequence

@CacheableTransform
abstract class NativeLibrariesReportTransform : TransformAction<TransformParameters.None> {

    private val jniRegEx = "jni/(.*)/(.*)\\.so".toRegex()

    @get:InputArtifact
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val inputAarFile: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        val file = inputAarFile.get().asFile
        val libraries = TreeMap<String, TreeSet<String>>()

        ZipFile(file).use { inputAar ->
            inputAar.stream().asSequence().forEach {
                val (abi, library) = jniRegEx.matchEntire(it.name)?.destructured ?: return@forEach

                libraries.getOrPut(library, ::TreeSet).add(abi)
            }
        }

        outputs.file("${file.name}.jni.yaml").outputStream().use {
            yaml.encodeToStream<NativeLibrary>(libraries, it)
        }
    }

}
