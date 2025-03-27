-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Mar 25, 2025 at 03:28 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `p2pfilesharing`
--

-- --------------------------------------------------------

--
-- Table structure for table `fileowner`
--

CREATE TABLE `fileowner` (
  `user_id` int(11) NOT NULL,
  `file_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `fileowner`
--

INSERT INTO `fileowner` (`user_id`, `file_id`) VALUES
(1, 1),
(1, 5),
(2, 2),
(3, 3);

-- --------------------------------------------------------

--
-- Table structure for table `sharedfiles`
--

CREATE TABLE `sharedfiles` (
  `file_id` int(11) NOT NULL,
  `filename` varchar(255) NOT NULL,
  `size` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  `checksum` varchar(64) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sharedfiles`
--

INSERT INTO `sharedfiles` (`file_id`, `filename`, `size`, `user_id`, `checksum`) VALUES
(1, 'song.mp4', 2048000, 1, 'song'),
(2, 'test.txt', 1024, 2, 'test'),
(3, 'document.pdf', 512000, 3, 'document'),
(5, 'example.txt', 1024, 1, 'example');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`) VALUES
(2, 'Homer'),
(1, 'Jimmy'),
(3, 'Marge');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `fileowner`
--
ALTER TABLE `fileowner`
  ADD PRIMARY KEY (`user_id`,`file_id`),
  ADD KEY `file_id` (`file_id`);

--
-- Indexes for table `sharedfiles`
--
ALTER TABLE `sharedfiles`
  ADD PRIMARY KEY (`file_id`),
  ADD UNIQUE KEY `checksum` (`checksum`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `sharedfiles`
--
ALTER TABLE `sharedfiles`
  MODIFY `file_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `fileowner`
--
ALTER TABLE `fileowner`
  ADD CONSTRAINT `fileowner_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fileowner_ibfk_2` FOREIGN KEY (`file_id`) REFERENCES `sharedfiles` (`file_id`) ON DELETE CASCADE;

--
-- Constraints for table `sharedfiles`
--
ALTER TABLE `sharedfiles`
  ADD CONSTRAINT `sharedfiles_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
