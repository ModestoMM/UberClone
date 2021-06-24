package com.modesto.uberclone.models;

public class HistoryBooking {

    //LA ESTRUCTURA DE LA CLASE ES SIMILAR A LA DE CLIENTBOOKING SOLO QUE AGREGAMOS EL IDDELHITORIALBOOKING
    //Y TAMBIEN LAS DOS CALIFICACIONES DEL CLIENTE Y DEL CONDUCTOR
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
    double calificationclient;
    double calificationdriver;
    //Este campo lo utilizaremos para saber en que fecha y hora se creo el historial del viaje
    long timestamp;

    public HistoryBooking(){

    }

    //Se añade en el contructor el id HistoryBooking y lo que es la calificacion tanto del cliente y del conductor no se añaden
    //ya que no se sabria que calificacion van a asignar tanto el conductor como el cliente para eso tendriamos los metodos
    //get y setter
    public HistoryBooking(String idHistoryBooking,String id_Client, String id_Driver, String destination, String origin, String time, String km, String status, double oring_Lat, double oring_Lng, double destinationLat, double destinationLng) {
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public double getCalificationClient() {
        return calificationclient;
    }

    public void setCalificationClient(double calificationClient) {
        calificationclient = calificationClient;
    }

    public double getCalificationDriver() {
        return calificationdriver;
    }

    public void setCalificationDriver(double calificationDriver) {
        calificationdriver = calificationDriver;
    }
}
