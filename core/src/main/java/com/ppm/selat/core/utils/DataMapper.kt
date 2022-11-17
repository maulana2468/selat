package com.ppm.selat.core.utils

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.ppm.selat.core.domain.model.Car
import com.ppm.selat.core.domain.model.CarImage
import com.ppm.selat.core.domain.model.OrderData
import com.ppm.selat.core.domain.model.Spec

fun documentSnapshotToCar(data: DocumentSnapshot): Car {
    return Car(
        id = data.id,
        location =  data["location"].toString(),
        carImage = documentSnapshotToCarImage(data),
        carManufacturer = data["manufacturer"].toString(),
        carBrand = data["brand"].toString(),
        typeCar = data["type"].toString(),
        price = Integer.parseInt(data["price"].toString()),
        rating = data["rating"].toString().toDouble(),
        yearProduction = Integer.parseInt(data["year_prod"].toString()),
        available = Integer.parseInt(data["price"].toString()),
        spec = documentSnapshotToSpec(data),
        latLng = convertGeoPointToListDouble(data.getGeoPoint("latlng")!!),
    )
}

fun documentSnapshotToCarImage(data: DocumentSnapshot) : CarImage {
    val dataPhoto  = (data.data?.get("photo") as Map<*, *>)
    return CarImage(
        primaryPhoto = dataPhoto["primary_photo"].toString(),
        sidePhoto = dataPhoto["side_photo"].toString()
    )
}

fun documentSnapshotToSpec(data: DocumentSnapshot) : Spec {
    val dataSpec = (data.data?.get("spec") as Map<*, *>)
    return Spec(
        a = dataSpec["a"].toString(),
        b = dataSpec["b"].toString(),
        c = dataSpec["c"].toString(),
    )
}

fun convertGeoPointToListDouble(geoPoint: GeoPoint) : List<Double> {
    return listOf(geoPoint.latitude, geoPoint.longitude)
}

fun convertToListOrderData(doc: DocumentSnapshot) : List<OrderData> {
    Log.d("TransactionActivity", doc.toString())
    val mapOfOrder = doc.data as Map<*, *>
    val listOrderData = ArrayList<OrderData>()
    mapOfOrder.map {
        val orderData = it.value as Map<*, *>
        listOrderData.add(
            OrderData(
                id = orderData["id"] as String,
                idCar = orderData["carId"] as String,
                brand = orderData["brand"] as String,
                manufacturer = orderData["manufacturer"] as String,
                dateOrder = orderData["dateOrder"] as String,
                paymentTypeName =  orderData["paymentTypeName"] as String,
                price = (orderData["price"] as Long).toInt(),
                rentDays = (orderData["rentDays"] as Long).toInt(),
                paymentNumber = orderData["paymentNumber"] as String
            )
        )
    }
    return listOrderData
}