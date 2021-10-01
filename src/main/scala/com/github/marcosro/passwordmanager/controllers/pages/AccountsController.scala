package com.github.marcosro.passwordmanager.controllers.pages

import scala.util.control.NonFatal
import javafx.fxml.FXML
import javafx.application.Platform
import javafx.scene.layout.VBox
import javafx.scene.input.{KeyEvent, KeyCode}
import javafx.scene.control.RadioMenuItem
import javafx.collections.{FXCollections, ObservableList}
import scalafx.stage.FileChooser
import scalafx.scene.control.{Label, TextField, TableColumn, ToggleGroup}
import scalafxml.core.macros.sfxml
import scalafx.Includes._
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView
import com.github.marcosro.passwordmanager.models.Account
import com.github.marcosro.passwordmanager.models.storage.commands.Find
import com.github.marcosro.passwordmanager.models.storage.Storage
import com.github.marcosro.passwordmanager.models.crypto.Crypto
import com.github.marcosro.passwordmanager.services.{CryptoServices, AccountServices}
import com.github.marcosro.passwordmanager.controllers.forms.Form

/** A class represent a accounts controller
  *
  * Specify the `root`, `searchField`, `searchByField`, `accountsTable`, `roleColumn`, `userColumn`, `platformColumn`, `error` when creating a new account controller
  * @constructor Create a new accounts controller with `root`, `searchField`, `searchByField`, `accountsTable`, `roleColumn`, `userColumn`, `platformColumn` and `error`
  * @param root The root layout
  * @param searchByField The search selector
  * @param accountsTable The table to show accounts
  * @param roleColumn The role column to display account's role
  * @param userColumn The user column to display account's username
  * @param platformColumn The platform column to display account's platform
  * @param error The accounts controller's user output for errors
  */
@sfxml
class AccountsController(
    @FXML private val root: VBox,
    @FXML private val searchField: TextField,
    @FXML private val searchByField: ToggleGroup,
    @FXML private val accountsTable: MFXLegacyTableView[Account],
    @FXML private val roleColumn: TableColumn[Account, String],
    @FXML private val userColumn: TableColumn[Account, String],
    @FXML private val platformColumn: TableColumn[Account, String],
    @FXML private val error: Label
) {
  private val accounts =
    FXCollections.observableArrayList[
      Account
    ]() // The accounts to display in accountsTable

  // Settings
  roleColumn.cellValueFactory = { _.value.getRoleProperty }
  userColumn.cellValueFactory = { _.value.getUser.getNameProperty }
  platformColumn.cellValueFactory = { _.value.getPlatformProperty }
  accountsTable.setItems(accounts)

  /** Search accounts if the user pressed enter
    * @param event The searchField's key event
    */
  def onSearchAction(event: KeyEvent): Unit =
    Platform.runLater { () =>
      if (event.getCode() == KeyCode.ENTER) {
        if (searchField.getText().replaceAll(" +", "").nonEmpty) {
          setError(isError = false)
          findByField
        } else
          setError("Search field is empty...")
      }
    }

  /** Show append form
    */
  def onAppendAction: Unit = {
    val task = Form.Put()
    showFormAndWait(task)
  }

  /** Show edit form
    */
  def onEditAction: Unit = {
    val account = accountsTable.getSelectionModel().getSelectedItem()
    if (account != null) {
      val task = Form.Update(account)
      showFormAndWait(task)
    }
  }

  /** Show delete form
    */
  def onDeleteAction: Unit = {
    val account = accountsTable.getSelectionModel().getSelectedItem()
    if (account != null) {
      val task = Form.Delete(() => accounts.remove(account), account)
      showFormAndWait(task)
    }
  }

  /** Export a result of query to csv
   */
  def onExportToCSVAction: Unit = {
    val accountsCopy = accounts.toArray().toList.map(_.asInstanceOf[Account].copy)
    if(accountsCopy.nonEmpty)
      saveDialog match {
        case Right(path) => {
          setError(isError = false)
          exportToCSV(path, accountsCopy)
        }

        case Left(e: Storage.StorageError) =>
          setError(e.getMessage)
      }
  }

  /** A method to show user errors
    */
  private def setError(message: String = "", isError: Boolean = true): Unit = {
    error.setText(message)
    error.setVisible(isError)
  }

  /** Get a window parent
    * @return The account controller's window
    */
  private def window: javafx.stage.Window =
    root.getScene.getWindow.asInstanceOf[javafx.stage.Window]

  /** Display form and wait
    * @param task The form type like: put form or delete form
    */
  private def showFormAndWait(task: Form.Task): Unit = {
    val path = "/views/forms/Account.fxml"
    val title = "Account"
    Form.showAndWait(window, path, title, task)
  }

  /**
   */
  private def fileChooser: FileChooser =
    new FileChooser {
      title = "Path to CSV export"
      extensionFilters ++= Seq(
        new FileChooser.ExtensionFilter("CSV file", "*.csv")
      )
    }

  /**
   */
  private def stage: javafx.stage.Stage =
    root.getScene.getWindow.asInstanceOf[javafx.stage.Stage]

  /**
   */
  private def saveDialog: Either[Storage.StorageError, String] =
    try {
      val file = fileChooser.showSaveDialog(stage)
      if (file != null)
        Right(file.getAbsolutePath())
      else
        Left(new Storage.StorageError("Not selected save location"))
    } catch {
      case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
    }

  /** Gets a searchByField's value
    * @return Gets searchByField's value
    */
  private def field: Find.Field = {
    val radioButton =
      searchByField.getSelectedToggle().asInstanceOf[RadioMenuItem]
    if (radioButton != null)
      radioButton.getText() match {
        case "Role"     => Find.AccountByRole
        case "Category" => Find.AccountByCategory
        case "Project"  => Find.AccountByProject
        case "Platform" => Find.AccountByPlatform
        case _          => Find.AccountByRole
      }
    else
      Find.AccountByRole
  }

  /** Processing a findByField query
    */
  private def findByField: Unit = {
    val program = AccountServices.findByField(field, searchField.getText())
    AccountServices.interpreter.run(program) match {
      case Right(rows) => {
        setError(isError = false)
        accounts.clear()
        accountsTable.refresh()
        rows.foreach(accounts.add)
      }

      case Left(e) => setError(e.getMessage)
    }
  }
 
  /** Export accounts to CSV file
   * @param path The path to store your accounts
   * @param accountsToExport The account to export
   */
  private def exportToCSV(path: String, accountsToExport: List[Account]): Unit = {
    val crypto = CryptoServices.point(accountsToExport) >>= {rows =>
      val passwords = rows.map(_.getUser.getPassword)
      CryptoServices.decryptMany(passwords) >>= {decryptedPasswords =>
        accountsToExport.zip(decryptedPasswords).foreach {
          case (account, decryptedPassword) => {
            account.getUser.setPassword(decryptedPassword)
          }
        }

        CryptoServices.point(accountsToExport)
      }
    }

    val execution = CryptoServices.interpreter.run(crypto).flatMap {rows =>
      val program = AccountServices.exportToCSV(path, rows)
      AccountServices.interpreter.run(program)
    } 

    // Handle execution result
    execution match {
      case Right(_) =>
        setError(isError = false)

      case Left(e: Crypto.CryptoError) =>
        setError(e.getMessage)

      case Left(e: Storage.StorageError) =>
        setError(e.getMessage)

      case Left(_) =>
        setError("Unknow error...")
    }
  }
}
