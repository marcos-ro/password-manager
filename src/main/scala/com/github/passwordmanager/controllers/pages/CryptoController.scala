package com.github.passwordmanager.controllers.pages

import scala.util.control.NonFatal
import javafx.fxml.FXML
import javafx.scene.layout.VBox
import scalafx.scene.control.TextArea
import scalafx.stage.FileChooser
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import com.github.passwordmanager.models.crypto.Crypto
import com.github.passwordmanager.services.CryptoServices

/** A class represent a crypto controller
  *
  * Specify the `root` and `terminal` when creating a new crypto controller
  * @constructor Create a new crypto controller with `root` and `terminal`
  * @param root The crypto controller's root layout
  * @param terminal The crypto controller's terminal for show user error or other things...
  */
@sfxml
class CryptoController(
    @FXML private val root: VBox,
    @FXML private val terminal: TextArea
) {

  /** On click on generate key pair button
    */
  def onGenerateKeyPairAction: Unit = {
    log("Open file browser...")
    val execution = saveDialog.flatMap { path =>
      CryptoServices.setPath(path)
      val program = CryptoServices.generateKeyPair >>= (_ =>
        CryptoServices.point(s"Key pair are genearted in $path")
      )

      CryptoServices.interpreter.run(program)
    }

    execution match {
      case Right(message) => log(message)
      case Left(e)        => log(e.getMessage)
    }
  }

  /** On click load key pair button
    */
  def onLoadKeyPairAction: Unit = {
    log("Open file browser...")
    val execution = openDialog.map { path =>
      CryptoServices.setPath(path)
      s"Key pair are loaded from $path"
    }

    execution match {
      case Right(message) => log(message)
      case Left(e)        => log(e.getMessage)
    }
  }

  /** For show messages form crypto terminal
    * @param message The message a show for users prefix default is {{{"hola" == ">> hola"}}}
    */
  private def log(message: String): Unit =
    if (terminal.getText().replaceAll(" +", "").nonEmpty) {
      val beforeLog = terminal.getText()
      terminal.text = s"$beforeLog\n>> $message"
    } else
      terminal.setText(f">> $message")

  /** Gets file chooser
    * @return scalafx.stage.fileChooser
    */
  private def fileChooser: FileChooser =
    new FileChooser {
      title = "Save key pairs"
      extensionFilters ++= Seq(
        new FileChooser.ExtensionFilter("Key pairs", "*.jkey")
      )
    }

  /** Gets stage
    * @return javafx.stage.Stage
    */
  private def stage: javafx.stage.Stage =
    root.getScene.getWindow.asInstanceOf[javafx.stage.Stage]

  /** Show save dialog for save key pairs
    * @return Either[Crypto.CryptoError, String] when String is abosulte path to key pairs
    */
  private def saveDialog: Either[Crypto.CryptoError, String] = {
    try {
      val file = fileChooser.showSaveDialog(stage)
      if (file != null)
        Right(file.getAbsolutePath())
      else
        Left(new Crypto.CryptoError("Not selected save location"))
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    }
  }

  /** Show a open dialog for load key pairs
    * @return Either[Crypto.CryptoError, String] when String is a abosule path to key pairs
    */
  private def openDialog: Either[Crypto.CryptoError, String] = {
    try {
      val file = fileChooser.showOpenDialog(stage)
      if (file != null)
        Right(file.getAbsolutePath())
      else
        Left(new Crypto.CryptoError("Not <file>.jkey selected"))
    } catch {
      case NonFatal(e) => Left(new Crypto.CryptoError(e.getMessage()))
    }
  }
}
