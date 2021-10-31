package com.github.marcosro.passwordmanager.components

import javafx.scene.control.{Button, TableCell}

/** A class represent a button into table view
  *
  * Specify the `label` and `event` when creating a new action button table cell
  * @constructor Create a new action button table cell with `label` and `event`
  * @param label The button label
  * @param event The button action if the user clicked in her
  */
class ActionButtonTableCell[T](
    private val label: String,
    private val event: T => Unit
) extends TableCell[T, T] {
  private val button = new Button(label)
  button.setOnAction(_ => event(currentItem))
  button.getStyleClass().addAll("flat-border", "green-button")

  override def updateItem(t: T, empty: Boolean): Unit = {
    super.updateItem(t, empty)
    if (empty)
      setGraphic(null)
    else
      setGraphic(button)
  }

  /** Return current value of table
    */
  private def currentItem: T = getTableView().getItems().get(getIndex())
}
