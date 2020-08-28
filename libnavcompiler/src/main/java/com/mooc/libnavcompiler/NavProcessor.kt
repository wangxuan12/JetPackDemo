package com.mooc.libnavcompiler

import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mooc.libnavannotation.ActivityDestination
import com.mooc.libnavannotation.FragmentDestination
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.mooc.libnavannotation.ActivityDestination", "com.mooc.libnavannotation.FragmentDestination")
class NavProcessor : AbstractProcessor() {
    private var messager: Messager? = null
    private var filer: Filer? = null
    private val OUTPUT_FILE_NAME = "destination.json"

    override fun init(p0: ProcessingEnvironment?) {
        super.init(p0)
        messager = p0?.messager
        filer = p0?.filer
    }

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        val fragmentElements = roundEnv?.getElementsAnnotatedWith(FragmentDestination::class.java)
        val activityElements = roundEnv?.getElementsAnnotatedWith(ActivityDestination::class.java)

        if (!fragmentElements.isNullOrEmpty() || !activityElements.isNullOrEmpty()) {
            val destMap = HashMap<String, JsonObject>()
            handleDestination(fragmentElements, FragmentDestination::class.java, destMap)
            handleDestination(activityElements, ActivityDestination::class.java, destMap)

            //app/src/main/assets
            val resource = filer?.createResource(StandardLocation.CLASS_OUTPUT, "", OUTPUT_FILE_NAME)
            val resourcePath = resource?.toUri()?.path
            val appPath = resourcePath?.substring(0, resourcePath.indexOf("app") + 4)
            val assetsPath = appPath + "src/main/assets"

            val file = File(assetsPath)
            if (!file.exists()) file.mkdirs()

            val outputFile = File("$assetsPath/$OUTPUT_FILE_NAME")
            if (outputFile.exists()) outputFile.delete()
            outputFile.createNewFile()

            val content = Gson().toJson(destMap)
            OutputStreamWriter(FileOutputStream(outputFile), "UTF-8").use {
                it.write(content)
                it.flush()
            }
//            var fos : FileOutputStream? = null
//            var writer: OutputStreamWriter? = null
//            try {
//                fos = FileOutputStream(outputFile)
//                writer = OutputStreamWriter(fos, "UTF-8")
//                writer.write(content)
//                writer.flush()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            } finally {
//                fos?.close()
//                writer?.close()
//            }
        }
        return true
    }

    private fun handleDestination(
        elements: Set<Element>?,
        annotationClazz: Class<out Annotation>,
        destMap: java.util.HashMap<String, JsonObject>
    ) {
        elements?.forEach { element ->
            val typeElement: TypeElement = element as TypeElement
            var pageUrl: String? = null
            var needLogin = false
            var asStarter = false
            val clazzName = typeElement.qualifiedName.toString()
            val id = Math.abs(clazzName.hashCode())
            var isFragment = false;

            val annotation = typeElement.getAnnotation(annotationClazz)
            if (annotation is FragmentDestination) {
                pageUrl = annotation.pageUrl
                needLogin = annotation.needLogin
                asStarter = annotation.asStarter
                isFragment = true
            } else if (annotation is ActivityDestination) {
                pageUrl = annotation.pageUrl
                needLogin = annotation.needLogin
                asStarter = annotation.asStarter
                isFragment = false
            }

            if (destMap.containsKey(pageUrl)) {
                messager?.printMessage(Diagnostic.Kind.ERROR, "不同的页面不允许使用相同的pageUrl: $clazzName")
            } else {
                val jsonObject = JsonObject()
                jsonObject.addProperty("id", id)
                jsonObject.addProperty("clazzName", clazzName)
                jsonObject.addProperty("pageUrl", pageUrl)
                jsonObject.addProperty("needLogin", needLogin)
                jsonObject.addProperty("asStarter", asStarter)
                jsonObject.addProperty("isFragment", isFragment)
                pageUrl?.also { destMap[it] = jsonObject }
            }
        }
    }
}