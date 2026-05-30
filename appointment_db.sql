-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: May 28, 2026 at 05:37 AM
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
-- Database: `appointment_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `appointments`
--

CREATE TABLE `appointments` (
  `id` int(11) NOT NULL,
  `student_id` int(11) NOT NULL,
  `teacher_id` int(11) NOT NULL,
  `date` date NOT NULL,
  `time` time NOT NULL,
  `status` enum('pending','approved','rejected','cancelled','completed') NOT NULL DEFAULT 'pending',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `purpose` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `appointments`
--

INSERT INTO `appointments` (`id`, `student_id`, `teacher_id`, `date`, `time`, `status`, `notes`, `created_at`, `purpose`) VALUES
(1, 3, 2, '2026-05-11', '09:00:00', 'approved', 'nice', '2026-05-11 01:09:42', NULL),
(2, 3, 4, '2026-05-11', '08:30:00', 'rejected', 'sorry hindi ka pumasa', '2026-05-11 01:20:29', NULL),
(3, 7, 2, '2026-11-26', '12:39:00', 'rejected', 'sybao', '2026-05-11 02:00:37', NULL),
(4, 7, 6, '2026-05-11', '11:00:00', 'cancelled', NULL, '2026-05-11 03:25:07', NULL),
(5, 9, 4, '2026-05-13', '14:00:00', 'rejected', 'fda ka na saken boiii', '2026-05-11 03:39:26', NULL),
(6, 9, 5, '2026-05-12', '16:30:00', 'pending', NULL, '2026-05-11 03:39:38', NULL),
(7, 9, 2, '2026-05-15', '13:00:00', 'pending', NULL, '2026-05-11 03:39:52', NULL),
(8, 7, 4, '2026-05-11', '12:30:00', 'approved', '10/10 you passed', '2026-05-11 03:55:22', NULL),
(9, 7, 5, '2026-05-12', '12:30:00', 'approved', 'yes', '2026-05-11 03:55:52', NULL),
(10, 7, 5, '2026-05-14', '09:00:00', 'pending', NULL, '2026-05-11 08:15:56', NULL),
(11, 7, 4, '2026-05-16', '09:00:00', 'completed', '', '2026-05-11 08:45:50', NULL),
(12, 7, 4, '2026-05-21', '15:00:00', 'pending', NULL, '2026-05-21 06:33:22', NULL),
(13, 7, 5, '2026-05-21', '15:30:00', 'pending', NULL, '2026-05-21 06:41:11', NULL),
(14, 7, 5, '2026-05-22', '13:00:00', 'pending', NULL, '2026-05-21 07:46:50', NULL),
(15, 7, 5, '2026-05-27', '11:00:00', 'pending', NULL, '2026-05-27 03:12:13', NULL),
(16, 7, 5, '2026-05-27', '10:00:00', 'pending', NULL, '2026-05-27 03:13:19', NULL),
(17, 7, 4, '2026-05-27', '08:30:00', 'pending', NULL, '2026-05-27 03:59:22', NULL),
(18, 7, 5, '2026-05-27', '12:00:00', 'pending', NULL, '2026-05-27 04:25:23', NULL),
(19, 7, 2, '2026-05-27', '10:00:00', 'pending', NULL, '2026-05-27 05:39:47', NULL),
(20, 7, 5, '2026-05-27', '09:00:00', 'pending', NULL, '2026-05-27 06:04:38', NULL),
(21, 7, 4, '2026-05-27', '13:30:00', 'pending', NULL, '2026-05-27 06:04:47', NULL),
(22, 7, 5, '2026-05-27', '08:00:00', 'pending', NULL, '2026-05-27 06:05:05', NULL),
(23, 7, 5, '2026-05-27', '14:00:00', 'pending', NULL, '2026-05-27 06:08:47', NULL),
(24, 7, 4, '2026-05-27', '15:00:00', 'pending', NULL, '2026-05-27 06:12:19', NULL),
(25, 7, 4, '2026-05-28', '10:30:00', 'pending', NULL, '2026-05-27 06:20:05', NULL),
(26, 7, 4, '2026-05-27', '11:30:00', 'pending', NULL, '2026-05-27 06:20:26', NULL),
(27, 7, 6, '2026-05-28', '14:30:00', 'pending', NULL, '2026-05-27 06:23:07', NULL),
(28, 7, 4, '2026-05-27', '08:00:00', 'pending', NULL, '2026-05-27 06:34:53', NULL),
(29, 7, 4, '2026-05-28', '08:00:00', 'pending', NULL, '2026-05-27 07:51:17', NULL),
(30, 7, 4, '2026-05-28', '08:30:00', 'pending', NULL, '2026-05-28 03:31:20', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `notifications`
--

CREATE TABLE `notifications` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `message` text NOT NULL,
  `is_read` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `notifications`
--

INSERT INTO `notifications` (`id`, `user_id`, `message`, `is_read`, `created_at`) VALUES
(1, 2, 'New appointment request from student on 2026-05-11 at 09:00', 0, '2026-05-11 01:09:43'),
(2, 4, 'New appointment request from student on 2026-05-11 at 08:30', 0, '2026-05-11 01:20:29'),
(3, 3, 'Your appointment on 2026-05-11 has been rejected. Remarks: sorry hindi ka pumasa', 0, '2026-05-11 01:21:43'),
(4, 2, 'New appointment request from Tristan A. Madi Jr. on 2026-11-26 at 11:99', 0, '2026-05-11 02:00:37'),
(5, 3, 'Your appointment on 2026-05-11 has been completed', 0, '2026-05-11 03:00:39'),
(6, 3, 'Your appointment on 2026-05-11 has been approved. Remarks: nice', 0, '2026-05-11 03:03:46'),
(7, 7, 'Your appointment on 2026-11-26 has been rejected. Remarks: sybao', 0, '2026-05-11 03:05:28'),
(8, 6, 'New appointment request from Tristan A. Madi Jr. on 2026-05-11 at 11:00', 0, '2026-05-11 03:25:07'),
(9, 4, 'New appointment request from Crystal Dela Santa on 2026-05-13 at 14:00', 0, '2026-05-11 03:39:26'),
(10, 5, 'New appointment request from Crystal Dela Santa on 2026-05-12 at 16:30', 0, '2026-05-11 03:39:38'),
(11, 2, 'New appointment request from Crystal Dela Santa on 2026-05-15 at 13:00', 0, '2026-05-11 03:39:52'),
(12, 4, 'New appointment request from Tristan A. Madi Jr. on 2026-05-11 at 12:30', 0, '2026-05-11 03:55:22'),
(13, 5, 'New appointment request from Tristan A. Madi Jr. on 2026-05-12 at 12:30', 0, '2026-05-11 03:55:52'),
(14, 7, 'Your appointment on 2026-05-12 has been approved. Remarks: yes', 0, '2026-05-11 03:58:15'),
(15, 5, 'New appointment request from Tristan A. Madi Jr. on 2026-05-14 at 09:00', 0, '2026-05-11 08:15:56'),
(16, 4, 'New appointment request from Tristan A. Madi Jr. on 2026-05-16 at 09:00', 0, '2026-05-11 08:45:50'),
(17, 7, 'Your appointment on 2026-05-11 has been approved. Remarks: 10/10 you passed', 0, '2026-05-11 08:47:36'),
(18, 9, 'Your appointment on 2026-05-13 has been rejected. Remarks: fda ka na saken boiii', 0, '2026-05-11 08:47:47'),
(19, 7, 'Your appointment on 2026-05-16 has been completed', 0, '2026-05-11 08:47:53'),
(20, 4, 'New appointment request from Tristan A. Madi Jr. on 2026-05-21 at 15:00', 0, '2026-05-21 06:33:22'),
(21, 5, 'New appointment request from Tristan A. Madi Jr. on 2026-05-21 at 15:30', 0, '2026-05-21 06:41:11'),
(22, 5, 'New appointment request from Tristan A. Madi Jr. on 2026-05-22 at 13:00', 0, '2026-05-21 07:46:50'),
(23, 5, 'New appointment request [Consultation purposes] from Tristan A. Madi Jr. on 2026-05-27 at 11:00', 0, '2026-05-27 03:12:13'),
(24, 5, 'New appointment request [Clinic purposes] from Tristan A. Madi Jr. on 2026-05-27 at 10:00', 0, '2026-05-27 03:13:19');

-- --------------------------------------------------------

--
-- Table structure for table `teacher_availability`
--

CREATE TABLE `teacher_availability` (
  `id` int(11) NOT NULL,
  `teacher_id` int(11) NOT NULL,
  `day_of_week` tinyint(4) NOT NULL,
  `slot_time` time NOT NULL,
  `is_available` tinyint(1) NOT NULL DEFAULT 1,
  `max_students` int(11) NOT NULL DEFAULT 5
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `teacher_availability`
--

INSERT INTO `teacher_availability` (`id`, `teacher_id`, `day_of_week`, `slot_time`, `is_available`, `max_students`) VALUES
(1, 4, 0, '08:00:00', 1, 5),
(2, 4, 0, '08:30:00', 1, 5),
(3, 4, 0, '09:00:00', 1, 5),
(4, 4, 0, '09:30:00', 1, 5),
(5, 4, 0, '10:00:00', 1, 5),
(6, 4, 0, '10:30:00', 1, 5),
(7, 4, 0, '11:00:00', 1, 5),
(8, 4, 0, '11:30:00', 1, 5),
(9, 4, 0, '12:00:00', 1, 5),
(10, 4, 0, '12:30:00', 1, 5),
(11, 4, 0, '13:00:00', 1, 5),
(12, 4, 0, '13:30:00', 1, 5),
(13, 4, 0, '14:00:00', 1, 5),
(14, 4, 0, '14:30:00', 1, 5),
(15, 4, 0, '15:00:00', 1, 5),
(16, 4, 0, '15:30:00', 1, 5),
(17, 4, 0, '16:00:00', 1, 5),
(18, 4, 0, '16:30:00', 1, 5),
(19, 4, 0, '17:00:00', 1, 5),
(20, 4, 0, '17:30:00', 1, 5),
(21, 4, 1, '08:00:00', 1, 5),
(22, 4, 1, '08:30:00', 1, 5),
(23, 4, 1, '09:00:00', 1, 5),
(24, 4, 1, '09:30:00', 1, 5),
(25, 4, 1, '10:00:00', 1, 5),
(26, 4, 1, '10:30:00', 1, 5),
(27, 4, 1, '11:00:00', 1, 5),
(28, 4, 1, '11:30:00', 1, 5),
(29, 4, 1, '12:00:00', 1, 5),
(30, 4, 1, '12:30:00', 1, 5),
(31, 4, 1, '13:00:00', 1, 5),
(32, 4, 1, '13:30:00', 1, 5),
(33, 4, 1, '14:00:00', 1, 5),
(34, 4, 1, '14:30:00', 1, 5),
(35, 4, 1, '15:00:00', 1, 5),
(36, 4, 1, '15:30:00', 1, 5),
(37, 4, 1, '16:00:00', 1, 5),
(38, 4, 1, '16:30:00', 1, 5),
(39, 4, 1, '17:00:00', 1, 5),
(40, 4, 1, '17:30:00', 1, 5),
(41, 4, 2, '08:00:00', 1, 5),
(42, 4, 2, '08:30:00', 1, 5),
(43, 4, 2, '09:00:00', 1, 5),
(44, 4, 2, '09:30:00', 1, 5),
(45, 4, 2, '10:00:00', 1, 5),
(46, 4, 2, '10:30:00', 1, 5),
(47, 4, 2, '11:00:00', 1, 5),
(48, 4, 2, '11:30:00', 1, 5),
(49, 4, 2, '12:00:00', 1, 5),
(50, 4, 2, '12:30:00', 1, 5),
(51, 4, 2, '13:00:00', 1, 5),
(52, 4, 2, '13:30:00', 1, 5),
(53, 4, 2, '14:00:00', 1, 5),
(54, 4, 2, '14:30:00', 1, 5),
(55, 4, 2, '15:00:00', 1, 5),
(56, 4, 2, '15:30:00', 1, 5),
(57, 4, 2, '16:00:00', 1, 5),
(58, 4, 2, '16:30:00', 1, 5),
(59, 4, 2, '17:00:00', 1, 5),
(60, 4, 2, '17:30:00', 1, 5),
(61, 4, 3, '08:00:00', 0, 2),
(62, 4, 3, '08:30:00', 1, 6),
(63, 4, 3, '09:00:00', 0, 1),
(64, 4, 3, '09:30:00', 0, 5),
(65, 4, 3, '10:00:00', 0, 5),
(66, 4, 3, '10:30:00', 0, 5),
(67, 4, 3, '11:00:00', 0, 5),
(68, 4, 3, '11:30:00', 0, 5),
(69, 4, 3, '12:00:00', 0, 5),
(70, 4, 3, '12:30:00', 0, 5),
(71, 4, 3, '13:00:00', 0, 5),
(72, 4, 3, '13:30:00', 0, 5),
(73, 4, 3, '14:00:00', 0, 5),
(74, 4, 3, '14:30:00', 1, 5),
(75, 4, 3, '15:00:00', 0, 5),
(76, 4, 3, '15:30:00', 0, 5),
(77, 4, 3, '16:00:00', 0, 5),
(78, 4, 3, '16:30:00', 0, 5),
(79, 4, 3, '17:00:00', 0, 5),
(80, 4, 3, '17:30:00', 0, 5),
(81, 4, 4, '08:00:00', 0, 5),
(82, 4, 4, '08:30:00', 0, 5),
(83, 4, 4, '09:00:00', 0, 5),
(84, 4, 4, '09:30:00', 0, 5),
(85, 4, 4, '10:00:00', 1, 3),
(86, 4, 4, '10:30:00', 1, 3),
(87, 4, 4, '11:00:00', 1, 3),
(88, 4, 4, '11:30:00', 1, 3),
(89, 4, 4, '12:00:00', 1, 3),
(90, 4, 4, '12:30:00', 1, 3),
(91, 4, 4, '13:00:00', 1, 5),
(92, 4, 4, '13:30:00', 1, 1),
(93, 4, 4, '14:00:00', 1, 3),
(94, 4, 4, '14:30:00', 1, 3),
(95, 4, 4, '15:00:00', 1, 3),
(96, 4, 4, '15:30:00', 1, 2),
(97, 4, 4, '16:00:00', 0, 5),
(98, 4, 4, '16:30:00', 0, 5),
(99, 4, 4, '17:00:00', 0, 5),
(100, 4, 4, '17:30:00', 0, 5);

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('student','teacher','admin') NOT NULL DEFAULT 'student',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `name`, `email`, `password`, `role`, `created_at`) VALUES
(1, 'Admin', 'admin@school.com', 'admin123', 'admin', '2026-05-11 01:06:59'),
(2, 'teacher', 'teacher@school.com', 'teacher123', 'teacher', '2026-05-11 01:07:00'),
(3, 'student', 'student@school.com', 'student123', 'student', '2026-05-11 01:07:00'),
(4, 'Marlon Berces', 'marlon@school.com', 'marlon123', 'teacher', '2026-05-11 01:12:40'),
(5, 'Armilyn', 'armilyn@school.com', 'armilyn123', 'teacher', '2026-05-11 01:13:25'),
(6, 'Yheng Sanchez', 'yheng@school.com', 'yheng123', 'teacher', '2026-05-11 01:17:16'),
(7, 'Tristan A. Madi Jr.', 'tristan@school.com', 'tristan123', 'student', '2026-05-11 01:56:33'),
(8, 'kiel coching', 'kiel@school.com', 'kiel123', 'student', '2026-05-11 01:56:55'),
(9, 'Crystal Dela Santa', 'crystal@gmail.com', 'crystal123', 'student', '2026-05-11 03:38:16');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `appointments`
--
ALTER TABLE `appointments`
  ADD PRIMARY KEY (`id`),
  ADD KEY `student_id` (`student_id`),
  ADD KEY `teacher_id` (`teacher_id`);

--
-- Indexes for table `notifications`
--
ALTER TABLE `notifications`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `teacher_availability`
--
ALTER TABLE `teacher_availability`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_teacher_slot` (`teacher_id`,`day_of_week`,`slot_time`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `appointments`
--
ALTER TABLE `appointments`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `notifications`
--
ALTER TABLE `notifications`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=25;

--
-- AUTO_INCREMENT for table `teacher_availability`
--
ALTER TABLE `teacher_availability`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=401;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `appointments`
--
ALTER TABLE `appointments`
  ADD CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `notifications`
--
ALTER TABLE `notifications`
  ADD CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `teacher_availability`
--
ALTER TABLE `teacher_availability`
  ADD CONSTRAINT `fk_avail_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
