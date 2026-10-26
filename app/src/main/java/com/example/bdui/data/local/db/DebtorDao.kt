package com.example.bdui.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.bdui.data.model.db.DebtEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtorDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(debt: DebtEntity): Long

    @Query("SELECT * FROM debtors WHERE id = :id LIMIT 1")
    suspend fun getDebtorById(id: String): DebtEntity?

    @Query("""
        SELECT * FROM debtors
        WHERE userId = :userId
        AND name = :name
        AND telegramNick IS :telegramNick
        AND isMine = :isMine
        LIMIT 1
    """)
    suspend fun findDebt(
        userId: String,
        name: String,
        telegramNick: String?,
        isMine: Boolean
    ): DebtEntity?

    @Query("""
        SELECT * FROM debtors
        WHERE userId = :userId
        AND name = :name
        AND telegramNick IS :telegramNick
        LIMIT 1
    """)
    suspend fun findAnyDebt(
        userId: String,
        name: String,
        telegramNick: String?
    ): DebtEntity?

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Query("SELECT * FROM debtors ORDER BY loanDate DESC")
    fun getAllDebtors(): Flow<List<DebtEntity>>

    @Query("DELETE FROM debtors WHERE id = :id")
    suspend fun deleteDebtor(id: String)

    @Query("DELETE FROM debtors WHERE debtAmount = 0")
    suspend fun deleteZeroDebtors()
}