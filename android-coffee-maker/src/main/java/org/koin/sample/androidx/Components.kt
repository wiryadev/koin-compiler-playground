package org.koin.sample.androidx

import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single

interface TaskDatasource

@Single
@Qualifier("local")
class LocalDatasource() : TaskDatasource

@Single
@Qualifier("remote")
class RemoteDatasource() : TaskDatasource

@Single
class Repository(
    @Qualifier("local")
    val local : TaskDatasource,
    @Qualifier("remote")
    val remote : TaskDatasource
)