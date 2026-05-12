-- Initial data for Bender Bot database

INSERT IGNORE INTO `frequent_service` (`name`, `port`, `short_io_url`, `short_io_link_id`, `status`) VALUES
('bender', 80, 'https://bender.short.io', 'abcabcabcabcabcabc', 'ENABLED'),
('immich', 2283, 'https://immich.short.io', 'aaabbbcccaaabbbccc', 'ENABLED'),
('nextcloud', 8080, 'https://nextcloud.short.io', 'abababcccababcc', 'ENABLED'),
('spliit', 3003, 'https://spliit.short.io', 'cbacbacbacbacbacba', 'ENABLED'),
('pingvin-share', 3000, 'https://pingvin.short.io', 'aabbccaabbccaabbcc', 'ENABLED');
