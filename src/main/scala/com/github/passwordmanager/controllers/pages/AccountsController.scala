package com.github.passwordmanager.controllers.pages

import javafx.fxml.FXML
import javafx.application.Platform
import javafx.scene.layout.VBox
import javafx.scene.input.{KeyEvent, KeyCode}
import javafx.scene.control.RadioMenuItem
import javafx.collections.{FXCollections, ObservableList}
import scalafx.scene.control.{Label, TextField, TableColumn, ToggleGroup}
import scalafxml.core.macros.sfxml
import io.github.palexdev.materialfx.controls.legacy.MFXLegacyTableView
import com.github.passwordmanager.models.Account
import com.github.passwordmanager.models.storage.commands.Find
import com.github.passwordmanager.services.AccountServices
import com.github.passwordmanager.controllers.forms.Form

/**
 * A class represent a accounts controller
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
  private val accounts = FXCollections.observableArrayList[Account]() // The accounts to display in accountsTable

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

  /** Show report form
   */
  def onReportAction: Unit = {
    val account = accountsTable.getSelectionModel().getSelectedItem()
    if (account != null) {
      val task = Form.Report(account)
      showFormAndWait(task)
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

  /** Gets a searchByField's value
   * @return Gets searchByField's value
   */
  private def field: Find.Field = {
    val radioButton = searchByField.getSelectedToggle().asInstanceOf[RadioMenuItem]
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
}
