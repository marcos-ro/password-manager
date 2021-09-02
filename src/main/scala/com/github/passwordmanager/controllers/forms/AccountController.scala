package com.github.passwordmanager.controllers.forms

import java.util.UUID
import javafx.fxml.FXML
import javafx.application.Platform
import scalafx.scene.layout.VBox
import scalafx.scene.control.{Label, Button}
import io.github.palexdev.materialfx.controls.MFXTextField
import scalafxml.core.macros.sfxml
import com.github.passwordmanager.models.{Account, User}
import com.github.passwordmanager.models.storage.Storage
import com.github.passwordmanager.models.crypto.Crypto
import com.github.passwordmanager.services.{CryptoServices, AccountServices}

/** A class represent a account controller
  *
  * Specify the `root`, `roleForm`, `roleField`, `platformForm`, `platformField`, `projectForm`, `projectField`, `categoryForm`, `categoryField`, `usernameForm`, `usernameField`, `passwordForm`, `passwordField`, `action` and `error` when creating a new account controller
  * @constructor Create a new account controller with `root`, `roleForm`, `roleField`, `platformForm`, `platformField`, `projectForm`, `projectField`, `categoryForm`, `categoryField`, `usernameForm`, `usernameField`, `passwordForm`, `passwordField`, `action` and `error`
  * @param root The account's root layout
  * @param roleForm The role's form layout
  * @param roleField The role's form content
  * @param platformForm The platform's form layout
  * @param platformField The platform's form content
  * @param projectForm The project's form layout
  * @param projectField The project's form content
  * @param categoryForm The category's form layout
  * @param categoryField The category's form content
  * @param usernameForm The username's form layout
  * @param usernameField The username's form content
  * @param passwordForm The password's form layout
  * @param passwordField The password's form content
  * @param action The account controller's action like: put or delete or update...
  * @param error The account controller's user output for errors
  */

@sfxml
class AccountController(
    @FXML private val root: VBox,
    @FXML private val roleForm: VBox,
    @FXML private val roleField: MFXTextField,
    @FXML private val platformForm: VBox,
    @FXML private val platformField: MFXTextField,
    @FXML private val projectForm: VBox,
    @FXML private val projectField: MFXTextField,
    @FXML private val categoryForm: VBox,
    @FXML private val categoryField: MFXTextField,
    @FXML private val usernameForm: VBox,
    @FXML private val usernameField: MFXTextField,
    @FXML private val passwordForm: VBox,
    @FXML private val passwordField: MFXTextField,
    @FXML private val action: Button,
    @FXML private val error: Label
) extends Form {
  private var accountOpt: Option[Account] =
    None // Current account if your operation is put this value is const None

  /** Make custom font with task
    */
  override def make(task: Form.Task): Unit =
    task match {
      case Form.Put() =>
        Platform.runLater { () =>
          randomPassword
          action.getStyleClass().remove("red-button")
          action.getStyleClass().add("green-button")
          action.setText("Register")
          action.setOnAction(_ => validation(put))
        }

      case Form.Update(account: Account) =>
        Platform.runLater { () =>
          accountOpt = Some(account)
          setAccount(account)
          action.getStyleClass().remove("red-button")
          action.getStyleClass().add("green-button")
          action.setText("Update")
          action.setOnAction(_ => validation(update))
        }

      case Form.Delete(remove, account: Account) =>
        Platform.runLater { () =>
          accountOpt = Some(account)
          setAccount(account)
          disableFields
          action.setText("Delete")
          action.setOnAction(_ => delete(remove))
        }

      case Form.Report(account: Account) =>
        Platform.runLater { () =>
          setAccount(account)
          disableFields
          action.setVisible(false)
        }

      case _ => {
        setError("The task to build not exists...")
        action.setVisible(false)
      }
    }

  /** Close this window
    */
  private def close: Unit =
    root.getScene.getWindow.asInstanceOf[javafx.stage.Stage].close()

  /** Write account object into all fields
    * @param account The account
    */
  private def setAccount(account: Account): Unit = {
    val user = account.getUser // extract user

    // set values into fields
    roleField.setText(account.getRole)
    platformField.setText(account.getPlatform)
    projectField.setText(account.getProject)
    categoryField.setText(account.getCategory)
    usernameField.setText(user.getName)

    // decrypt your encrypted password
    decrypt(user.getPassword)
  }

  /** Verfiy if current account exists and apply a function
    * @param f The function to apply if account exists
    */
  private def accountOptValidation(f: Account => Unit): Unit =
    accountOpt match {
      case Some(account) => f(account)
      case None          => setError("Your account to update not exists...")
    }

  /** Update accountOpt fields
    */
  private def updateAccountOpt: Unit =
    accountOptValidation { account =>
      account.setRole(roleField.getText())
      account.setPlatform(platformField.getText())
      account.setProject(projectField.getText())
      account.setCategory(categoryField.getText())
      account.getUser.setName(usernameField.getText())
    }

  /** A method to show user errors
    * @param message The error message
    * @param isError The error is containt error
    */
  private def setError(message: String = "", isError: Boolean = true): Unit = {
    error.setText(message)
    error.setVisible(isError)
  }

  /** A get a false if any field is empty
    * @return all fields is empty
    */
  private def isEmpty: Boolean =
    roleField.getText().replaceAll(" +", "").isEmpty ||
      platformField.getText().replaceAll(" +", "").isEmpty ||
      projectField.getText().replaceAll(" +", "").isEmpty ||
      categoryField.getText().replaceAll(" +", "").isEmpty ||
      usernameField.getText().replaceAll(" +", "").isEmpty ||
      passwordField.getText().replace(" +", "").isEmpty

  private def disableFields: Unit = {
    roleField.setDisable(true)
    platformField.setDisable(true)
    projectField.setDisable(true)
    categoryField.setDisable(true)
    usernameField.setDisable(true)
    passwordField.setDisable(true)
  }

  /** Check if empty all fields
    */
  def validation(f: => Unit): Unit =
    if (!isEmpty) {
      setError(isError = false)
      f
    } else
      setError("Empty fields..")

  /** A clear all fields
    */
  private def clear: Unit = {
    roleField.setText("")
    platformField.setText("")
    projectField.setText("")
    categoryField.setText("")
    usernameField.setText("")
    randomPassword
  }

  /** Generate your account model
    * @return account model
    */
  private def retrieve: Account = {
    val uuid = UUID.randomUUID().toString
    val role = roleField.getText()
    val platform = platformField.getText()
    val project = projectField.getText()
    val category = categoryField.getText()
    val username = usernameField.getText()
    val password = passwordField.getText()
    val user = new User(username, password)
    new Account(uuid, user, role, platform, project, category)
  }

  /** Generate and set your plain password
    * @param encryptedPassword The encrypted password
    */
  private def decrypt(encryptedPassword: String): Unit = {
    val crypto = CryptoServices.decrypt(encryptedPassword)
    CryptoServices.interpreter.run(crypto) match {
      case Right(plain) =>
        passwordField.setText(plain)
      case Left(e) =>
        setError(e.getMessage)
    }
  }

  /** Generate random password with length 12
    */
  private def randomPassword: Unit = {
    val crypto = CryptoServices.randomPassword(12)
    CryptoServices.interpreter.run(crypto) match {
      case Right(password) => passwordField.setText(password)
      case Left(e)         => setError(e.getMessage)
    }
  }

  /** Register and store your account in database
    */
  private def put: Unit = {
    // Return your account with account's encrypted password (monad)
    val crypto = CryptoServices.point(retrieve) >>= { account =>
      CryptoServices.encrypt(account.getUser.getPassword) >>= { password =>
        account.getUser.setPassword(password)
        CryptoServices.point(account)
      }
    }

    // Return your account with account's encrypted password (result)
    val execution = CryptoServices.interpreter.run(crypto).flatMap { account =>
      val program = AccountServices.put(account)
      AccountServices.interpreter.run(program)
    }

    // Handle execution result
    execution match {
      case Right(_) => {
        clear
        setError(isError = false)
      }

      case Left(e: Crypto.CryptoError) =>
        setError(e.getMessage)

      case Left(e: Storage.StorageError) =>
        setError(e.getMessage)

      case Left(_) =>
        setError("Unknow error...")
    }
  }

  /** Update your account in database
    */
  private def update: Unit =
    accountOptValidation { account =>
      // Return your account with account's encrypted password (monad)
      val crypto = CryptoServices.point(account) >>= { cryptoAccount =>
        CryptoServices.encrypt(passwordField.getText()) >>= { password =>
          cryptoAccount.getUser.setPassword(password)
          CryptoServices.point(cryptoAccount)
        }
      }

      // Return your account with account's encrypted password (result)
      val execution =
        CryptoServices.interpreter.run(crypto).flatMap { cryptoAccount =>
          val program = AccountServices.update(cryptoAccount)
          val encryptedPassword = cryptoAccount.getUser.getPassword
          account.getUser.setPassword(encryptedPassword)
          AccountServices.interpreter.run(program)
        }

      // Handle execution result
      execution match {
        case Right(_) => {
          updateAccountOpt
          setError(isError = false)
        }

        case Left(e: Crypto.CryptoError) =>
          setError(e.getMessage)

        case Left(e: Storage.StorageError) =>
          setError(e.getMessage)

        case Left(_) =>
          setError("Unknow error...")
      }
    }

  /** Warning: delete account in database
    */
  private def delete(remove: () => Unit): Unit =
    accountOptValidation { account =>
      val program = AccountServices.delete(account.getUUID)
      AccountServices.interpreter.run(program) match {
        case Right(_) => {
          remove()
          close
        }

        case Left(e) =>
          setError(e.getMessage)
      }
    }
}
