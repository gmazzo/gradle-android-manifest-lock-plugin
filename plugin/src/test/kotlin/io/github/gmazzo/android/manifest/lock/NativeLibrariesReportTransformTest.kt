package io.github.gmazzo.android.manifest.lock

import com.android.build.gradle.internal.publishing.AndroidArtifacts.ArtifactType
import io.github.gmazzo.android.manifest.lock.AndroidManifestLockPlugin.Companion.JNI_REPORT_ARTIFACT_TYPE
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import org.gradle.api.Action
import org.gradle.api.artifacts.type.ArtifactTypeDefinition.ARTIFACT_TYPE_ATTRIBUTE
import org.gradle.api.problems.Problem
import org.gradle.api.problems.ProblemId
import org.gradle.api.problems.ProblemSpec
import org.gradle.api.problems.internal.AdditionalDataBuilderFactory
import org.gradle.api.problems.internal.InternalProblem
import org.gradle.api.problems.internal.InternalProblemBuilder
import org.gradle.api.problems.internal.InternalProblemReporter
import org.gradle.api.problems.internal.InternalProblemSpec
import org.gradle.api.problems.internal.InternalProblems
import org.gradle.api.problems.internal.ProblemsProgressEventEmitterHolder
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.internal.reflect.Instantiator
import org.gradle.kotlin.dsl.creating
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.registerTransform
import org.gradle.kotlin.dsl.repositories
import org.gradle.testfixtures.ProjectBuilder

class NativeLibrariesReportTransformTest {

    private val tempDir = File(System.getenv("TEMP_DIR")!!, "transformTest").apply {
        deleteRecursively()
        mkdirs()
    }

    private val configuration = with(
        ProjectBuilder.builder()
            .withProjectDir(tempDir)
            .withGradleUserHomeDir(tempDir.resolve(".home"))
            .build()
    ) {
        gradleIssue31862Workaround()

        val debugDependencies by configurations.creating

        dependencies {
            registerTransform(NativeLibrariesReportTransform::class) {
                from.attribute(ARTIFACT_TYPE_ATTRIBUTE, ArtifactType.AAR.type)
                to.attribute(ARTIFACT_TYPE_ATTRIBUTE, JNI_REPORT_ARTIFACT_TYPE)
            }

            debugDependencies("androidx.camera:camera-core:1.4.1")
        }

        repositories {
            mavenCentral()
            google()
        }

        debugDependencies.incoming
            .artifactView {
                attributes.attribute(
                    ARTIFACT_TYPE_ATTRIBUTE,
                    JNI_REPORT_ARTIFACT_TYPE
                )
            }
    }

    @Test
    fun `transform produces a yaml file`() {
        val files = configuration.files.associate { it.name to it.readText().trim() }

        assertEquals(
            mapOf(
                "camera-core-1.4.1.aar.jni.yaml" to """
                    libimage_processing_util_jni:
                    - arm64-v8a
                    - armeabi-v7a
                    - x86
                    - x86_64
                    libsurface_util_jni:
                    - arm64-v8a
                    - armeabi-v7a
                    - x86
                    - x86_64
                    """.trimIndent().trim(),
            ),
            files,
        )
    }

    // TODO workaround for
    private fun gradleIssue31862Workaround() =
        ProblemsProgressEventEmitterHolder.init(object : InternalProblems {

            override fun getInternalReporter() = object : InternalProblemReporter {

                override fun report(
                    problem: Problem,
                    id: OperationIdentifier
                ) {
                    TODO("Not yet implemented")
                }

                override fun internalCreate(action: Action<in InternalProblemSpec>): InternalProblem {
                    TODO("Not yet implemented")
                }

                override fun create(
                    problemId: ProblemId,
                    action: Action<in ProblemSpec>
                ): Problem {
                    TODO("Not yet implemented")
                }

                override fun report(
                    problemId: ProblemId,
                    spec: Action<in ProblemSpec>
                ) {
                    TODO("Not yet implemented")
                }

                override fun report(problem: Problem) {
                    TODO("Not yet implemented")
                }

                override fun report(problems: Collection<Problem?>) {
                    TODO("Not yet implemented")
                }

                override fun throwing(
                    exception: Throwable,
                    problemId: ProblemId,
                    spec: Action<in ProblemSpec>
                ): RuntimeException {
                    TODO("Not yet implemented")
                }

                override fun throwing(
                    exception: Throwable,
                    problem: Problem
                ): RuntimeException {
                    TODO("Not yet implemented")
                }

                override fun throwing(
                    exception: Throwable,
                    problems: Collection<Problem?>
                ): RuntimeException {
                    TODO("Not yet implemented")
                }

            }

            override fun getAdditionalDataBuilderFactory(): AdditionalDataBuilderFactory {
                TODO("Not yet implemented")
            }

            override fun getInstantiator(): Instantiator {
                TODO("Not yet implemented")
            }

            override fun getProblemBuilder(): InternalProblemBuilder {
                TODO("Not yet implemented")
            }

            override fun getReporter() = getInternalReporter()

        })
}
