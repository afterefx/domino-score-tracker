package com.domino.scoretracker.data.repository

import com.domino.scoretracker.data.local.dao.PlayerDao
import com.domino.scoretracker.data.mapper.toDomain
import com.domino.scoretracker.data.mapper.toEntity
import com.domino.scoretracker.domain.model.Player
import com.domino.scoretracker.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAllPlayers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getPlayerById(id: Long): Player? =
        playerDao.getPlayerById(id)?.toDomain()

    override suspend fun createPlayer(player: Player): Long =
        playerDao.insertPlayer(player.toEntity())

    override suspend fun updatePlayer(player: Player) =
        playerDao.updatePlayer(player.toEntity())

    override suspend fun deletePlayer(player: Player) =
        playerDao.deletePlayer(player.toEntity())

    override suspend fun isNameTaken(name: String, excludeId: Long): Boolean =
        playerDao.countByName(name.trim(), excludeId) > 0
}
