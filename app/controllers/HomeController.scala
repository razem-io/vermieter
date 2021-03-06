package controllers

import better.files.File
import javax.inject._
import play.api.mvc._
import services.TemplateConverterService

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, templateConverterService: TemplateConverterService) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    templateConverterService.convert(File("./conf/templates/mietvertrag.template.tex"), File("./ign.data/output"))
    Ok(views.html.index())
  }
}
