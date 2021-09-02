package com.github.passwordmanager.models.storage

package commands {
  import com.github.passwordmanager.models.Account

  /** A trait represent a storage a command like: put, delete or other command
    * take any type to return by storage interpreter
    */
  sealed trait Command[_]

  /** A class represent a put command (similar to SQL insert) take a `t`, this is a value to put on storage
    *
    * Sepcify the t value (is generic) when create a new put operation
    * then access the fields like this:
    * {{{
    * val user = new User("test-user", "test-password")
    * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
    * val put = Put(account)
    * put.t
    * }}}
    *
    * @constructor Create a put operation with `t`
    * @param t Value to put on storage
    */
  case class Put[T](t: T) extends Command[Int]

  /** A class represent a delete command (similiar to SQL delete) take a `k` when `k` is a unique value
    *
    * Specify the `k` when create a new delete command
    * then access the fields like this:
    * {{{
    * val uniqueValue = "d4ce02e0-7dc8-4a5f-9c79-a576298f631c"
    * val delete = Delete(uniqueValue)
    * delete.k
    * }}}
    * @constructor Create a new delete command with `k`
    * @param k Unique key to delete record on storage
    */
  case class Delete[K](k: K) extends Command[Int]

  /** A class represent a update command (simliar to SQL update) take a `k` when `k` is a unique value stored on database or file and `t` when `t` is a value to update or replace old `t`
    *
    * Specify the `k` and `t` when create a new update command
    * then access the fields like this:
    * {{{
    * val uniqueValue = "d4ce02e0-7dc8-4a5f-9c79-a576298f631c"
    * val user = new User("test-user", "test-password")
    * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
    * val update = Update(uniqueValue, account)
    * update.k
    * update.t
    * }}}
    * @constructor Create a new update with a `k` and `t`
    * @param k The unique value on stored record (similar to primary key database)
    * @param t The new record to update or replace old values
    */
  case class Update[T, K](k: K, t: T) extends Command[Int]

  /** A companion by class [[com.github.passwordmanager.models.storage.commands.FindAccountByField]]
    * define utils methods for find command like:
    * {{{
    * Find.AccountByRole      // Find account by role     field
    * Find.AccountByCategory //  Find account by category field
    * Find.AccountByProject  //  Find account by project  field
    * Find.AccountByPlatform //  Find account by platform field
    * }}}
    */
  object Find {

    /** A trait represent a field of record stored on database or file
      */
    sealed trait Field

    /** A class represent find by role field in find command
      */
    case object AccountByRole extends Field

    /** A class represent find by category field in find command
      */
    case object AccountByCategory extends Field

    /** A class represent find by project in find command
      */
    case object AccountByProject extends Field

    /** A class represent find by platform in find command
      */
    case object AccountByPlatform extends Field
  }

  /** A class represent a find account by id command (similar to SQL select) take a `id` when `id` is a unique value stored on database or file
    *
    * Specify `id` when create a new find account by id command
    * then access the fields like this:
    * {{{
    * val uniqueValue = "d4ce02e0-7dc8-4a5f-9c79-a576298f631c"
    * val find = FindAccountById(uniqueValue)
    * find.id
    * }}}
    * @constructor Create a new find account by id command with `id`
    * @param id The id is a unique value stored on database or file
    */
  case class FindAccountById(id: String) extends Command[Account]

  /** A class represent a find account by field command (similar to SQL select) take `field` when `field` is a enum into [[com.github.passwordmanager.models.storage.commands.Find]] companion and `value` is a search value to match on database or file
    *
    * Specify `field` and `value` when create a new find account by field command
    * then access the fields like this:
    * {{{
    * val find = FindAccountByField(Find.AccountByCategory, "Entreteniment")
    * find.field
    * find.value
    * }}}
    */
  case class FindAccountByField(field: Find.Field, value: String)
      extends Command[List[Account]]
}

package programs {
  import com.github.passwordmanager.models.storage.commands.Command

  /** A trait represent a storage program
    */
  sealed trait Program[T] { self =>
    def map[V](f: T => V): Program[V] =
      RunAndThen(self, (t: T) => Return(f(t)))

    def flatMap[V](f: T => Program[V]): Program[V] =
      RunAndThen(self, f)
  }

  /** A class represent a execution of command storage and return a `T` parameter of command storage
    *
    * Specify `command` with `T` parameter for know return type of execution when create a new executable storage command
    * then access the fields like this:
    * {{{
    * val user = new User("test-user", "test-password")
    * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
    * val execute = Execute(Put(account))
    * execute.command
    * }}}
    * @constructor Create a execute command storage with `command`
    * @param command The execute's command storage
    */
  case class Execute[T](command: Command[T]) extends Program[T]

  /** A class represent a execution sequence like example: execute x and with execution x apply sum x + 3
    *
    * Specify a `program` and `map` when create a new execution sequence
    * then access the fields like this:
    * {{{
    * val user = new User("test-user", "test-password")
    * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
    * val executionSequence= RunAndThen(Execute(Put(account)), (account: Account) => Return(account.getUUID))
    * executionSequence.program
    * executionSequence.map
    * }}}
    * @constructor Create execution sequence with `program` and `map`
    * @param program The program to execute and pass to result and apply map function
    * @param map  Transform to program execution result and map to other program
    */
  case class RunAndThen[T, V](program: Program[T], map: T => Program[V])
      extends Program[V]

  /** A class represent a return and nothing more
    *
    * Specify a `t` when create new return point
    * then access the fields like this:
    * {{{
    * val point = Return(10)
    * point.t
    * }}}
    * @constructor Create a return with t
    * @param t The value to return
    */
  case class Return[T](t: T) extends Program[T]
}

import com.github.passwordmanager.models.storage.programs.Program

/** A companion by class [[com.github.passwordmanager.models.storage.Storage]]
  * define utils method for storage like:
  * {{{
  * Storage.StorageError("Not exists a file") // Error
  * }}}
  */
object Storage {

  /** A class represent a storage error like: not exists database or not exists a file..
    */
  class StorageError(private val message: String) {

    /** Gets error message
      *  @return Error message
      */
    def getMessage: String =
      message
  }
}

/** A trait represent a storage interpreter
  */
trait Storage {

  /** A abstract storage interpreter that run storage programs
    *
    * @param program The storage's program
    * @return If execution of storage program is ok return `T` when other case return [[com.github.passwordmanager.models.storage.Storage.StorageError]]
    */
  def run[T](program: Program[T]): Either[Storage.StorageError, T]
}
