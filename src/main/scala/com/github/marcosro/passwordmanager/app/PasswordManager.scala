package com.github.marcosro.passwordmanager.app

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafxml.core.{NoDependencyResolver, FXMLView}

object PasswordManager extends JFXApp {
  private val path = getClass.getResource("/views/Home.fxml")
  private val root = FXMLView(path, NoDependencyResolver)

  stage = new JFXApp.PrimaryStage {
    title = "Password Manager"
    scene = new Scene(root)
  }

  stage.setResizable(false)
  stage.show()
}
