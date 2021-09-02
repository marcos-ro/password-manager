package com.github.passwordmanager.models.storage.nitrite

import scala.util.control.NonFatal
import com.github.passwordmanager.models.Account
import com.github.passwordmanager.models.storage.Storage
import com.github.passwordmanager.models.storage.commands._
import com.github.passwordmanager.models.storage.programs._

/** A companion by class [[com.github.passwordmanager.models.storage.nitrite.StorageNitrite]]
  * define utils methods like:
  * {{{
  * StorageNitrite.openOrCreate() // open a nitrite database
  * }}}
  */
private object StorageNitrite {
  import org.dizitart.no2.Nitrite

  /** Open a nitrite database in `path` specified
    * @param path Path to database file for nitrite
    * @return {{{Either[Storage.StorageError, Nitrite]}}} when Nitrite is a nitrite database instance for access utils method to manipulate data
    */
  def openOrCreate(
      path: String = "./lib/nitrite/password-manager.db"
  ): Either[Storage.StorageError, Nitrite] =
    try {
      val database =
        Nitrite.builder().filePath(path).compressed().openOrCreate()
      Right(database)
    } catch {
      case NonFatal(e) => Left(new Storage.StorageError(e.getMessage()))
    }
}

/** A class represent a storage interpreter for nitrite database, like put account in nitrite database
  *
  * ==Example==
  * {{{
  * val user = new User("test-user", "test-password")
  * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
  * val program = Execute(Put(account))
  * val nitrite = new StorageNitrite()
  * nitrite.run(program) // return Right(1)
  * }}}
  */
class StorageNitrite extends Storage {

  /** Take a storage program and return execution result
    * @param program Storage program to execute and produce a result
    * @return Either[Storage.StorageError, T] when T is a result of program execution
    */
  override def run[T](program: Program[T]): Either[Storage.StorageError, T] =
    program match {
      case RunAndThen(current, next) =>
        run(current).map(next).flatMap(run)
      case Execute(Put(account: Account)) =>
        AccountNitrite.put(account)
      case Execute(Delete(uuid: String)) =>
        AccountNitrite.delete(uuid)
      case Execute(Update(uuid: String, account: Account)) =>
        AccountNitrite.update(uuid, account)
      case Execute(FindAccountById(uuid)) =>
        AccountNitrite.findById(uuid)
      case Execute(FindAccountByField(field, value)) =>
        AccountNitrite.findByField(field, value)
      case Return(value) =>
        Right(value)
      case _ =>
        Left(new Storage.StorageError("Unknow storage program"))
    }
}
