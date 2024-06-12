package com.perikov.maven.abstractions
// TODO: TBD
// trait Caching:
//   type F[_]
//   type Resolver[-K, +V]
//   type EligibleForCaching[K, V] <: Resolver[K, V]
//   type Cached[-K, +V] <: Resolver[K, V]

//   type CanCache[K, V]

//   extension [K, V](resolver: Resolver[K, V]) def resolve: Cached[K, V]

//   extension [K, V](original: EligibleForCaching[K, V])(using CanCache[K, V])
//     def cached: F[Cached[K, V]]

//   extension [K, V](cached: Cached[K, V])
//     def uncached: F[Resolver[K, V]]
    
