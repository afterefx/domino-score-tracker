package com.domino.scoretracker.di

import com.domino.scoretracker.data.repository.GameRepositoryImpl
import com.domino.scoretracker.data.repository.PlayerRepositoryImpl
import com.domino.scoretracker.data.repository.RoundRepositoryImpl
import com.domino.scoretracker.data.repository.RoundScoreRepositoryImpl
import com.domino.scoretracker.data.repository.StatsRepositoryImpl
import com.domino.scoretracker.domain.repository.GameRepository
import com.domino.scoretracker.domain.repository.PlayerRepository
import com.domino.scoretracker.domain.repository.RoundRepository
import com.domino.scoretracker.domain.repository.RoundScoreRepository
import com.domino.scoretracker.domain.repository.StatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindRoundRepository(impl: RoundRepositoryImpl): RoundRepository

    @Binds
    @Singleton
    abstract fun bindRoundScoreRepository(impl: RoundScoreRepositoryImpl): RoundScoreRepository

    @Binds
    @Singleton
    abstract fun bindStatsRepository(impl: StatsRepositoryImpl): StatsRepository
}
