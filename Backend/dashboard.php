<?php
include 'config.php';

$conn = new mysqli($host, $user, $pass, $db);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$filter_rep_id = $_GET['rep_id'] ?? '';
$start_date = $_GET['start_date'] ?? '';
$end_date = $_GET['end_date'] ?? '';
?>

<!DOCTYPE html>
<html>
<head>
    <title>GPS Location Dashboard</title>
    <style>
        body { font-family: Arial; margin: 20px; background: #f5f5f5;}
        h2 { color: #333; }
        form { margin-bottom: 20px; }
        input, button {
            padding: 8px;
            font-size: 14px;
            margin-right: 10px;
        }
        table {
            width: 100%; border-collapse: collapse; background: white;
            box-shadow: 0 0 8px rgba(0,0,0,0.1);
        }
        th, td {
            padding: 10px; border-bottom: 1px solid #ccc; text-align: left;
        }
        th { background:rgb(0, 91, 151); color: white; }
        tr:hover { background-color: #f1f1f1; }
    </style>
</head>
<body>

<h2>GPS Location Dashboard</h2>

<!-- Filter Form -->
<form method="GET" action="">
    <label>Rep ID:</label>
    <input type="text" name="rep_id" value="<?= htmlspecialchars($filter_rep_id) ?>">

    <label>Start Date:</label>
    <input type="date" name="start_date" value="<?= htmlspecialchars($start_date) ?>">

    <label>End Date:</label>
    <input type="date" name="end_date" value="<?= htmlspecialchars($end_date) ?>">

    <button type="submit">Filter</button>
</form>

<table>
    <tr>
        <th>Rep ID</th>
        <th>Latitude</th>
        <th>Longitude</th>
        <th>Timestamp</th>
        <th>ClockOut Latitude</th>
        <th>ClockOut Longitude</th>
        <th>ClockOut Timestamp</th>

    </tr>

    <?php
    $sql = "SELECT rep_id, latitude, longitude, timestamp,clock_out_latitude,clock_out_longitude,clock_out_timestamp FROM locations WHERE 1=1";
    $params = [];
    $types = '';

    if ($filter_rep_id !== '') {
        $sql .= " AND rep_id = ?";
        $params[] = $filter_rep_id;
        $types .= 's';
    }

    if ($start_date !== '') {
        $sql .= " AND DATE(timestamp) >= ?";
        $params[] = $start_date;
        $types .= 's';
    }

    if ($end_date !== '') {
        $sql .= " AND DATE(timestamp) <= ?";
        $params[] = $end_date;
        $types .= 's';
    }

    $sql .= " ORDER BY timestamp DESC";

    $stmt = $conn->prepare($sql);
    if (!empty($params)) {
        $stmt->bind_param($types, ...$params);
    }
    $stmt->execute();
    $result = $stmt->get_result();

    if ($result->num_rows > 0) {
        while ($row = $result->fetch_assoc()) {
            echo "<tr>
                <td>{$row['rep_id']}</td>
                <td>{$row['latitude']}</td>
                <td>{$row['longitude']}</td>
                <td>{$row['timestamp']}</td>
                <td>{$row['clock_out_latitude']}</td>
                <td>{$row['clock_out_longitude']}</td>
                <td>{$row['clock_out_timestamp']}</td>
            </tr>";
        }
    } else {
        echo "<tr><td colspan='4'>No data found</td></tr>";
    }

    $conn->close();
    ?>
</table>

</body>
</html>