<?php

include 'config.php';

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    http_response_code(500);
    die("Connection failed: " . $conn->connect_error);
}

$rep_id = $_POST['rep_id'] ?? '';
$latitude = $_POST['latitude'] ?? '';
$longitude = $_POST['longitude'] ?? '';
$action = $_POST['action'] ?? 'clock_in'; // default to clock_in if not provided

if (!is_numeric($latitude) || !is_numeric($longitude) || empty($rep_id)) {
    http_response_code(400);
    echo "Invalid parameters";
    $conn->close();
    exit;
}

if ($action === 'clock_out') {
    $timestamp = date('Y-m-d H:i:s');

    $sql = "UPDATE locations 
            SET clock_out_latitude = ?, clock_out_longitude = ?, clock_out_timestamp = ?
            WHERE rep_id = ?
            ORDER BY timestamp DESC 
            LIMIT 1";

    $stmt = $conn->prepare($sql);
    $stmt->bind_param("ssss", $latitude, $longitude, $timestamp, $rep_id);

    if ($stmt->execute()) {
        http_response_code(200);
        echo "Clock out recorded successfully.";
    } else {
        http_response_code(500);
        echo "Error: " . $stmt->error;
    }

    $stmt->close();
} else {
    // default action: clock_in
    $stmt = $conn->prepare("INSERT INTO locations (rep_id, latitude, longitude) VALUES (?, ?, ?)");
    $stmt->bind_param("sdd", $rep_id, $latitude, $longitude);

    if ($stmt->execute()) {
        http_response_code(200);
        echo "Location saved (Clock In)";
    } else {
        http_response_code(500);
        echo "Error: " . $stmt->error;
    }

    $stmt->close();
}

$conn->close();
