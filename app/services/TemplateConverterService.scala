package services

import better.files.File
import com.google.inject.Inject
import com.typesafe.config.ConfigValue
import play.api.{Configuration, Logging}

import scala.util.matching.Regex

class TemplateConverterService @Inject()(configuration: Configuration) extends Logging {
  import TemplateConverterService._

  val landlordConf: Configuration = configuration.get[Configuration]("global")
  import collection.JavaConverters._
  val tenantConfigs: Seq[Configuration] = configuration.getConfigList("mieter").map(_.asScala).get
  val landlordMap: Map[String, ConfigValue] = landlordConf.entrySet.toMap

  def convert(templateFile: File, outputFolder: File): Unit = {
    val templateText = templateFile.contentAsString

    tenantConfigs.foreach(tenantConfig => {
      val t = tenantConfig.entrySet.toMap.mapKeys("mieter." + _) ++ landlordMap

      val renderedText = confRegex.replaceAllIn(templateText, m => {
        val confPath = m.group(1)

        val foundKey = t.keys.find(_ == confPath.toLowerCase())

        foundKey.map(t).map(_.unwrapped().toString).getOrElse("")
      })

      t.toSeq.sortBy(_._1).foreach(e => println(e._1 + " => " + e._2.unwrapped()))

      outputFolder.createDirectoryIfNotExists(createParents = true)
      val outputFile = File(outputFolder.pathAsString + "/" + tenantConfig.get[String]("name").toLowerCase.replace(' ', '_') + "-mietvertrag.tex")
      if(outputFile.exists) outputFile.delete()
      outputFile.writeText(renderedText)

      import sys.process._
      Process("pdflatex " + outputFile.name, outputFolder.toJava).!
    })
  }


}

object TemplateConverterService {
  private def confRegex: Regex = """§§([a-z.]*)§§""".r

  implicit class MapFunctions[A, B](val map: Map[A, B]) extends AnyVal {
    def mapKeys[A1](f: A => A1): Map[A1, B] = map.map({ case (a, b) => (f(a), b) })
  }
}
