package com.github.marcosro.passwordmanager.controllers.forms

/** A companion of class [[com.github.marcosro.passwordmanager.controllers.forms.Form]]
  */
object Form {
  import javafx.stage.Window
  import scalafx.Includes._
  import scalafx.stage.Stage
  import scalafx.scene.Scene
  import scalafxml.core.{NoDependencyResolver, FXMLLoader, FXMLView}

  /** A trait represent a task to build form
    */
  sealed trait Task

  /** A class represent a put task to build form
    */
  case class Put() extends Task

  /** A class represent a update task to build form
    * @param t This is a old value to replace
    */
  case class Update[T](t: T) extends Task

  /** A class represent a delete task to build form
    * @param remove This function remove item from main page
    * @param t This is a value to delete
    */
  case class Delete[T](remove: () => Unit, t: T) extends Task

  /** A show and wait form
    * @param path Form's path view
    * @param parent Form's window parent
    * @param name Form's name
    * @param task Form's task to build
    */
  def showAndWait(
      parent: Window,
      path: String,
      name: String,
      task: Task
  ): Unit = {
    val loader =
      new FXMLLoader(getClass.getResource(path), NoDependencyResolver)
    loader.load()
    val controller = loader.getController[Form]
    controller.make(task)

    val stage = new Stage {
      title = name
      scene = new Scene(loader.getRoot[javafx.scene.Parent])
    }

    stage.setResizable(false)
    stage.delegate.initOwner(parent)
    stage.delegate.initModality(javafx.stage.Modality.WINDOW_MODAL)
    stage.showAndWait()
  }
}

/** A trait represent a form like: register form, setting form and other things
  */
trait Form {

  /** Build your custom form like: make a put form or report form
    * @param task like makefile task to build like: build a put form
    */
  def make(task: Form.Task): Unit
}
