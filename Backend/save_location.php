<?php

include 'config.php';;

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    http_response_code(500);
    die("Connection failed: " . $conn->connect_error);
}

$rep_id = $_POST['rep_id'] ?? '';
$latitude = $_POST['latitude'] ?? '';
$longitude = $_POST['longitude'] ?? '';

// Validate latitude and longitude
if (!is_numeric($latitude) || !is_numeric($longitude) || empty($rep_id)) {
    http_response_code(400);
    echo "Invalid parameters";
    $conn->close();
    exit;
}

$stmt = $conn->prepare("INSERT INTO locations (rep_id, latitude, longitude) VALUES (?, ?, ?)");
$stmt->bind_param("sdd", $rep_id, $latitude, $longitude);

if ($stmt->execute()) {
    http_response_code(200);
    echo "Location saved";
} else {
    http_response_code(500);
    echo "Error: " . $stmt->error;
}

$stmt->close();
$conn->close();

?>
