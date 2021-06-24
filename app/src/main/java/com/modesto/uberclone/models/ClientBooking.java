package com.modesto.uberclone.models;

//En esta clase pondrmos los datos del cliente que observara el conductor como la posicion del cliente la distancia en que se encuentra
//El tiempo en llegar a su posicion etc.
public class ClientBooking {

    String idHistoryBooking;
    String id_Client;
    String id_Driver;
    String destination;
    String origin;
    String time;
    String km;
    String status;
    double oring_Lat;
    double oring_Lng;
    double destinationLat;
    double destinationLng;
    double price;

    public ClientBooking(){

    }

    public ClientBooking(String id_Client, String id_Driver, String destination, String origin, String time, String km, String status, double oring_Lat, double oring_Lng, double destinationLat, double destinationLng) {
        this.id_Client = id_Client;
        this.id_Driver = id_Driver;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.oring_Lat = oring_Lat;
        this.oring_Lng = oring_Lng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public ClientBooking(String idHistoryBooking, String id_Client, String id_Driver, String destination, String origin, String time, String km, String status, double oring_Lat, double oring_Lng, double destinationLat, double destinationLng) {
        this.idHistoryBooking=idHistoryBooking;
        this.id_Client = id_Client;
        this.id_Driver = id_Driver;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.oring_Lat = oring_Lat;
        this.oring_Lng = oring_Lng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
    }

    public ClientBooking(String idHistoryBooking, String id_Client, String id_Driver, String destination, String origin, String time, String km, String status, double oring_Lat, double oring_Lng, double destinationLat, double destinationLng, double price) {
        this.idHistoryBooking = idHistoryBooking;
        this.id_Client = id_Client;
        this.id_Driver = id_Driver;
        this.destination = destination;
        this.origin = origin;
        this.time = time;
        this.km = km;
        this.status = status;
        this.oring_Lat = oring_Lat;
        this.oring_Lng = oring_Lng;
        this.destinationLat = destinationLat;
        this.destinationLng = destinationLng;
        this.price = price;
    }

    public String getId_Client() {
        return id_Client;
    }

    public void setId_Client(String id_Client) {
        this.id_Client = id_Client;
    }

    public String getId_Driver() {
        return id_Driver;
    }

    public void setId_Driver(String id_Driver) {
        this.id_Driver = id_Driver;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getOring_Lat() {
        return oring_Lat;
    }

    public void setOring_Lat(double oring_Lat) {
        this.oring_Lat = oring_Lat;
    }

    public double getOring_Lng() {
        return oring_Lng;
    }

    public void setOring_Lng(double oring_Lng) {
        this.oring_Lng = oring_Lng;
    }

    public double getDestinationLat() {
        return destinationLat;
    }

    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }

    public double getDestinationLng() {
        return destinationLng;
    }

    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }

    public String getIdHistoryBooking() {
        return idHistoryBooking;
    }

    public void setIdHistoryBooking(String idHistoryBooking) {
        this.idHistoryBooking = idHistoryBooking;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
