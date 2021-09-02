package com.github.passwordmanager.controllers

import javafx.fxml.FXML
import scalafx.scene.layout.AnchorPane
import scalafxml.core.macros.sfxml

/** A class represent a home controller
  *
  * Specify the `accountsPage` and `cryptoPage` when creating a new home controller
  * @constructor Create a new home controller with a `accountPage` and `cryptoPage`
  * @param accountsPage The account page this pane contains resources/views/pages/Accounts.fxml
  * @param cryptoPage The crypto page this pane contains resources/views/pages/Crypto.fxml
  */
@sfxml
class HomeController(
    @FXML private val accountsPage: AnchorPane,
    @FXML private val cryptoPage: AnchorPane
) {

  /** On click in accounts page button
    */
  def onAccountsPageAction: Unit = {
    setVisiblePages(false)
    accountsPage.setVisible(true)
  }

  /** On click in crypto page button
    */
  def onCryptoPageAction: Unit = {
    setVisiblePages(false)
    cryptoPage.setVisible(true)
  }

  /** Set false in setVisible on all pages
    */
  private def setVisiblePages(visible: Boolean): Unit = {
    accountsPage.setVisible(visible)
    cryptoPage.setVisible(visible)
  }
}
