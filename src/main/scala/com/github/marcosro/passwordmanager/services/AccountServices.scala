package com.github.marcosro.passwordmanager.services

import com.github.marcosro.passwordmanager.models.Account
import com.github.marcosro.passwordmanager.models.storage.Storage
import com.github.marcosro.passwordmanager.models.storage.commands._
import com.github.marcosro.passwordmanager.models.storage.programs._
import com.github.marcosro.passwordmanager.models.storage.nitrite.StorageNitrite

/**  A class represent account services like:
  * {{{
  * val user = new User("test-user", "test-password")
  * val account = new Account("d4ce02e0-7dc8-4a5f-9c79-a576298f631c", user, "final user", "Netflix", "Watch series and other things", "Entreteniment")
  * val program = AccountServices.put(account)
  * AccountServices.interpreter.run(program) // return put result
  * }}}
  */
object AccountServices {

  /** Gets storage interpreter for processing storage programs
    *  @return Gets storage interpreter for processing storage programs
    */
  def interpreter: Storage =
    new StorageNitrite

  /** Gets put program (similar to SQL insert) to stored account into database or file
    * @param account The account to stored into database or file
    * @return Gets put program (similiar to SQL insert) to stored account into database or file
    */
  def put(account: Account): Program[Int] =
    Execute(Put(account))

  /** Gets update program (similar to SQL update) to replace account stored into database or file
    * @param account The account to replace old account into database or file
    * @return Gets update program (similar to SQL update) to replace account stored into database or file
    */
  def update(account: Account): Program[Int] =
    Execute(Update(account.getUUID, account))

  /** Gets delete program (similar to SQL delete) to delete account stored into database or file
    * @param uuid The account's uuid (universally unique identifier)
    * @return Getes delete program (similar to SQL delete) to delete account stored into database or file
    */
  def delete(uuid: String): Program[Int] =
    Execute(Delete(uuid))

  /** Gets find program (similar to SQL select) to find account stored into database or file by uuid (similar to SQL primary key)
    * @param uuid The account's uuid (universally unique identifier)
    * @return Gets find program (similar to SQL select) to find account stored into database or file by uuid (similar to SQL primary key)
    */
  def findById(uuid: String): Program[Account] =
    Execute(FindAccountById(uuid))

  /** Gets find program (similar to SQL select) to find account stored into database or file by field
    * @param field The field to filter search like: find this account by category
    * @param value The value to field to filter
    * @return Gets find program (similar to SQL select) to find account stored into database or file by field
    */
  def findByField(field: Find.Field, value: String): Program[List[Account]] =
    Execute(FindAccountByField(field, value))
}
