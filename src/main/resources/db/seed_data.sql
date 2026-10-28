-- Script phục hồi toàn bộ dữ liệu chuẩn cho audiotour_db
USE audiotour_db;

-- 1. Languages
INSERT INTO languages (id, code, name) VALUES
(1, 'vi', 'Tiếng Việt'),
(2, 'en', 'English'),
(3, 'zh', '中文'),
(4, 'ja', '日本語'),
(5, 'ko', '한국어'),
(6, 'ru', 'Русский'),
(7, 'fr', 'Français'),
(8, 'it', 'Italiano'),
(9, 'de', 'Deutsch')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Locations
INSERT INTO locations (id, name, address, latitude, longitude, radius_in_meters, priority, qr_code, image_url, parent_id) VALUES
(1, 'Trạm dừng xe buýt Khánh Hội', 'Đường Hoàng Diệu, Phường Khánh Hội, Quận 4, TP.HCM', 10.7602, 106.7031, 35.0, 2, 'QR-KHANH-HOI-01', 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600', NULL),
(2, 'Trạm dừng xe buýt Vĩnh Hội', 'Đường Khánh Hội, Phường Vĩnh Hội, Quận 4, TP.HCM', 10.7585, 106.7012, 30.0, 1, 'QR-VINH-HOI-01', 'https://images.unsplash.com/photo-1570125909232-eb263c188f7e?w=600', NULL),
(3, 'Trạm dừng xe buýt Xóm Chiếu', 'Đường Đoàn Văn Bơ, Phường Xóm Chiếu, Quận 4, TP.HCM', 10.7631, 106.7075, 30.0, 3, 'QR-XOM-CHIEU-01', 'https://images.unsplash.com/photo-1517649763962-0c623266ddc0?w=600', NULL),
(4, 'Dinh Độc Lập', '135 Nam Kỳ Khởi Nghĩa, Bến Thành, Quận 1, TP.HCM', 10.7770, 106.6953, 100.0, 5, 'QR-DINH-DOC-LAP-01', 'https://images.unsplash.com/photo-1583417319070-4a69db38a482?w=600', NULL),
(5, 'Phòng Khánh Tiết', 'Tầng trệt, Dinh Độc Lập', 10.7771, 106.6954, 15.0, 2, 'QR-PHONG-KHANH-TIET', 'https://images.unsplash.com/photo-1513694203232-719a280e022f?w=600', 4),
(6, 'Phòng Đại Yến', 'Tầng 1, Dinh Độc Lập', 10.7772, 106.6955, 15.0, 2, 'QR-PHONG-DAI-YEN', 'https://images.unsplash.com/photo-1541971875076-8f970d573be6?w=600', 4)
ON DUPLICATE KEY UPDATE name=VALUES(name), address=VALUES(address), latitude=VALUES(latitude), longitude=VALUES(longitude);

-- 3. Narrations (54 bản thuyết minh: 6 địa điểm x 9 ngôn ngữ)
INSERT INTO narrations (id, location_id, language_id, text_content, audio_url, duration_in_seconds) VALUES
-- Trạm Khánh Hội (loc 1)
(1, 1, 1, 'Chào mừng quý khách đến với trạm dừng xe buýt Khánh Hội, Quận 4. Vùng đất Khánh Hội xưa vốn là một cù lao trù phú bên dòng kênh Bến Nghé, gắn liền với thương cảng Sài Gòn sầm uất và sự giao lưu văn hóa của cư dân Nam Bộ từ thời khẩn hoang.', 'tram-khanh-hoi-vi.mp3', 90),
(2, 1, 2, 'Welcome to Khanh Hoi bus stop in District 4. Khanh Hoi was historically a fertile islet along Ben Nghe canal, closely associated with the bustling Saigon commercial port and the rich cultural exchange of Southern Vietnamese settlers since the pioneering era.', 'tram-khanh-hoi-en.mp3', 90),
(3, 1, 3, '欢迎来到第四郡庆会社交巴士站。昔日的庆会地区是滨艺运河畔一块肥沃的岛屿，自拓荒时期起便与繁华的西贡商港以及南部居民的多元文化交流紧密相连。', 'tram-khanh-hoi-zh.mp3', 80),
(4, 1, 4, 'カインホイ（Khanh Hoi）バス停へようこそ。かつてのカインホイ地域は、ベンゲー運河沿いの肥沃な中州であり、サイゴンの活気ある商業港や開拓時代からの南部住民の豊かな文化交流と深く結びついていました。', '1-ja.mp3', 85),
(25, 1, 5, '4군에 위치한 칸호이(Khanh Hoi) 버스 정류장에 오신 것을 환영합니다. 과거 칸호이 지역은 벤응에(Ben Nghe) 운하를 따라 형성된 비옥한 하중도였으며, 번화한 사이공 무역항 및 개척 시대부터 이어진 남부 베트남 주민들의 풍부한 문화 교류와 깊은 관련이 있습니다.', '1-ko.mp3', 85),
(26, 1, 6, 'Добро пожаловать на автобусную остановку Кханьхой в Районе 4. Исторически местность Кханьхой представляла собой плодородный остров вдоль канала Беннге, тесно связанный с оживленным торговым портом Сайгона и богатым культурным обменом жителей Южного Вьетнама со времен освоения земель.', '1-ru.mp3', 85),
(27, 1, 7, 'Bienvenue à l''arrêt de bus Khanh Hoi dans le 4e arrondissement. Historiquement, la région de Khanh Hoi était un îlot fertile le long du canal de Ben Nghe, étroitement lié au port de commerce animé de Saïgon et aux riches échanges culturels des colons du Sud depuis l''époque des pionniers.', '1-fr.mp3', 85),
(28, 1, 8, 'Benvenuti alla fermata dell''autobus di Khanh Hoi nel Distretto 4. Storicamente, l''area di Khanh Hoi era un fertile isolotto lungo il canale Ben Nghe, strettamente legato al vivace porto commerciale di Saigon e al ricco scambio culturale dei residenti del Vietnam meridionale fin dall''epoca pionieristica.', '1-it.mp3', 85),
(29, 1, 9, 'Willkommen an der Bushaltestelle Khanh Hoi im 4. Bezirk. Historisch gesehen war die Region Khanh Hoi eine fruchtbare Flussinsel entlang des Ben-Nghe-Kanals, eng verbunden mit dem geschäftigen Handelshafen von Saigon und dem reichen kulturellen Austausch der südvietnamesischen Bevölkerung seit der Pionierzeit.', '1-de.mp3', 85),

-- Trạm Vĩnh Hội (loc 2)
(5, 2, 1, 'Trạm dừng xe buýt Vĩnh Hội đưa quý khách đến với không gian giao thương sầm uất của Quận 4. Nơi đây từng là đầu mối vận chuyển hàng hóa đường thủy nhộn nhịp, nay là khu vực phát triển năng động với nhiều nét ẩm thực đường phố đặc sắc.', 'tram-vinh-hoi-vi.mp3', 85),
(6, 2, 2, 'Vinh Hoi bus stop takes you to the bustling trading hub of District 4. Once a lively waterway freight transport terminal, it is now a dynamic developing area famous for its distinctive and vibrant street food culture.', 'tram-vinh-hoi-en.mp3', 85),
(7, 2, 3, '永会社交巴士站带您走进第四郡繁华热闹的商贸天地。这里曾是水运货物集散的重要枢纽，如今已发展成为充满活力、汇聚丰富地道街头美食的特色街区。', 'tram-vinh-hoi-zh.mp3', 80),
(8, 2, 4, 'ヴィンホイ（Vinh Hoi）バス停は、第4区の活気ある交易空間へと皆様をご案内します。ここはかつて水上貨物輸送の賑やかな拠点であり、現在は特色豊かな屋台グルメが楽しめる活気あふれるエリアとなっています。', '2-ja.mp3', 85),
(30, 2, 5, '빈호이(Vinh Hoi) 버스 정류장은 4군의 활기찬 상업 중심지로 여러분을 안내합니다. 이곳은 한때 번화한 수로 화물 운송의 요충지였으며, 현재는 개성 넘치는 길거리 음식 문화로 유명한 역동적인 발전 지역입니다.', '2-ko.mp3', 85),
(31, 2, 6, 'Автобусная остановка Виньхой открывает путь к оживленному торговому центру Района 4. Когда-то бывший шумным узлом водного грузового транспорта, сегодня этот динамично развивающийся район славится своей самобытной и яркой культурой уличной еды.', '2-ru.mp3', 85),
(32, 2, 7, 'L''arrêt de bus Vinh Hoi vous conduit au cœur de la zone commerciale animée du 4e arrondissement. Autrefois un nœud très actif de fret fluvial, c''est aujourd''hui un quartier en plein essor réputé pour sa gastronomie de rue riche et authentique.', '2-fr.mp3', 85),
(33, 2, 8, 'La fermata dell''autobus di Vinh Hoi vi porta nel vivace centro commerciale del Distretto 4. Un tempo animato snodo per il trasporto merci via acqua, oggi è un''area dinamica in forte sviluppo, famosa per la sua caratteristica e vivace cucina di strada.', '2-it.mp3', 85),
(34, 2, 9, 'Die Bushaltestelle Vinh Hoi führt Sie in das geschäftige Handelszentrum des 4. Bezirks. Einst ein lebhafter Umschlagplatz für den Binnenschiffstransport, ist dieser dynamisch wachsende Stadtteil heute berühmt für seine vielfältige und lebendige Street-Food-Kultur.', '2-de.mp3', 85),

-- Trạm Xóm Chiếu (loc 3)
(9, 3, 1, 'Trạm dừng Xóm Chiếu là nơi lưu giữ nét văn hóa làng nghề truyền thống nổi tiếng từ thế kỷ trước. Nơi đây từng là trung tâm dệt chiếu cói nức tiếng phương Nam, nay trở thành thiên đường ẩm thực đường phố sầm uất bậc nhất của Thành phố Hồ Chí Minh.', 'tram-xom-chieu-vi.mp3', 95),
(10, 3, 2, 'Xom Chieu bus stop preserves the traditional handicraft village culture renowned since the last century. Once a famous center for woven sedge mats in the South, today it has transformed into one of the most vibrant street food paradises in Ho Chi Minh City.', 'tram-xom-chieu-en.mp3', 95),
(11, 3, 3, '照村巴士站承载着上世纪闻名遐迩的传统手工艺村文化。这里曾是越南南方著名的草席编织中心，如今已蜕变成为胡志明市最繁华热闹的街头美食天堂之一。', 'tram-xom-chieu-zh.mp3', 85),
(12, 3, 4, 'ソムチエウ（Xom Chieu）バス停は、前世紀から続く伝統工芸村の文化を今に伝えています。かつては南部で名高いイグサのゴザ織りの中心地であり、現在はホーチミン市で最も賑やかなストリートフードの天国となっています。', '3-ja.mp3', 90),
(35, 3, 5, '솜찌에우(Xom Chieu) 정류장은 지난 세기부터 유명했던 전통 공예 마을의 문화를 간직하고 있는 곳입니다. 과거 남부에서 명성을 떨쳤던 왕골 돗자리 짜기의 중심지였으며, 현재는 호치민시에서 가장 번화한 길거리 음식의 천국 중 하나로 변모했습니다.', '3-ko.mp3', 90),
(36, 3, 6, 'Остановка Сомтьеу бережно хранит культуру традиционной ремесленной деревни, известной с прошлого века. Некогда славившийся на весь юг центр плетения тростниковых циновок, сегодня превратился в один из самых оживленных уголков уличной кухни в Хошимине.', '3-ru.mp3', 90),
(37, 3, 7, 'L''arrêt Xom Chieu préserve la culture traditionnelle d''un village artisanal réputé depuis le siècle dernier. Jadis grand centre de tissage de nattes de jonc du Sud, le lieu s''est transformé aujourd''hui en l''un des paradis de cuisine de rue les plus bouillonnants d''Hô Chi Minh-Ville.', '3-fr.mp3', 90),
(38, 3, 8, 'La fermata di Xom Chieu custodisce la memoria del tradizionale villaggio artigianale celebre fin dal secolo scorso. Un tempo rinomato centro per la tessitura di stuoie di canna nel Sud, oggi si è trasformato in uno dei più vivaci paradisi dello street food di Ho Chi Minh City.', '3-it.mp3', 90),
(39, 3, 9, 'Die Haltestelle Xom Chieu bewahrt die traditionsreiche Handwerkskultur, die seit dem letzten Jahrhundert berühmt ist. Einst ein bekanntes Zentrum für das Weben von Seggenmatten im Süden, hat es sich heute zu einem der lebendigsten Street-Food-Paradiese von Ho-Chi-Minh-Stadt gewandelt.', '3-de.mp3', 90),

-- Dinh Độc Lập (loc 4)
(13, 4, 1, 'Dinh Độc Lập, nay là Di tích Lịch sử Dinh Độc Lập, tọa lạc tại số 135 đường Nam Kỳ Khởi Nghĩa, Quận 1, Thành phố Hồ Chí Minh. Công trình được khởi công xây dựng vào năm 1962 trên nền Dinh Norodom cũ, theo đồ án thiết kế của kiến trúc sư tài hoa Ngô Viết Thụ. Nơi đây là chứng nhân lịch sử đánh dấu thời khắc hòa bình, thống nhất non sông vào trưa ngày 30 tháng 4 năm 1975.', 'dinh-doc-lap-vi.mp3', 150),
(14, 4, 2, 'The Independence Palace, now the Independence Palace Historical Relic, is located at 135 Nam Ky Khoi Nghia Street, District 1, Ho Chi Minh City. Construction commenced in 1962 on the site of the former Norodom Palace, designed by the talented architect Ngo Viet Thu. This landmark stands as a historical witness marking the momentous era of peace and national reunification at noon on April 30, 1975.', 'dinh-doc-lap-en.mp3', 150),
(15, 4, 3, '独立宫位于胡志明市第一郡南圻起义街135号，1962年在诺罗敦宫旧址上建成，由建筑师吴曰树设计。1975年4月30日中午，这里见证了越南南北统一的历史时刻。', 'dinh-doc-lap-zh.mp3', 140),
(16, 4, 4, '統一会堂（旧独立宮殿）は、ホーチミン市第1区ナムキーコイギア通り135番地に位置しています。才能ある建築家ゴー・ヴィエット・トゥ（Ngo Viet Thu）の設計により、1962年に旧ノロドム宮殿の跡地に建設が開始されました。ここは1975年4月30日正午の平和と国家統一の瞬間を刻んだ歴史の証人です。', '4-ja.mp3', 145),
(40, 4, 5, '통일궁(구 독립궁)은 호치민시 1군 남키코이응이어(Nam Ky Khoi Nghia) 거리 135번지에 위치해 있습니다. 천재 건축가 응오비엣투(Ngo Viet Thu)의 설계로 1962년 옛 노로돔 궁전 터에 착공되었습니다. 이곳은 1975년 4월 30일 정오, 조국의 평화와 통일을 맞이한 역사적 순간을 증언하는 유적지입니다.', '4-ko.mp3', 145),
(41, 4, 6, 'Дворец Независимости, ныне Исторический памятник Дворец Воссоединения, расположен по адресу: улица Намки Хойнгя, 135, Район 1, Хошимин. Строительство началось в 1962 году на месте прежнего дворца Нородом по проекту талантливого архитектора Нго Вьет Тху. Этот монумент является живым свидетелем мира и национального воссоединения в полдень 30 апреля 1975 года.', '4-ru.mp3', 145),
(42, 4, 7, 'Le Palais de l''Indépendance, aujourd''hui monument historique national, se dresse au 135 rue Nam Ky Khoi Nghia, 1er arrondissement de Hô Chi Minh-Ville. Les travaux ont débuté en 1962 sur l''emplacement de l''ancien palais Norodom, d''après les plans du talentueux architecte Ngo Viet Thu. Ce lieu demeure un témoin historique marquant l''avènement de la paix et de la réunification nationale le 30 avril 1975 à midi.', '4-fr.mp3', 145),
(43, 4, 8, 'Il Palazzo dell''Indipendenza, oggi monumento storico, si trova al civico 135 di via Nam Ky Khoi Nghia, nel Distretto 1 di Ho Chi Minh City. La costruzione ebbe inizio nel 1962 sul sito dell''antico Palazzo Norodom, secondo il progetto del brillante architetto Ngo Viet Thu. Questo luogo rappresenta un testimone storico fondamentale della pace e della riunificazione nazionale a mezzogiorno del 30 aprile 1975.', '4-it.mp3', 145),
(44, 4, 9, 'Der Wiedervereinigungspalast (ehemaliger Unabhängigkeitspalast) befindet sich in der Nam-Ky-Khoi-Nghia-Straße 135 im 1. Bezirk von Ho-Chi-Minh-Stadt. Die Errichtung begann 1962 auf dem Gelände des früheren Norodom-Palastes nach Entwürfen des renommierten Architekten Ngo Viet Thu. Das Bauwerk ist ein bedeutender historischer Zeuge des Friedens und der nationalen Wiedervereinigung am Mittag des 30. April 1975.', '4-de.mp3', 145),

-- Phòng Khánh Tiết (loc 5)
(17, 5, 1, 'Chào mừng quý khách đến với Phòng Khánh Tiết tại Dinh Độc Lập. Đây là căn phòng có diện tích lớn nhất trong Dinh, được thiết kế trang trọng với sức chứa lên đến hơn năm trăm người. Căn phòng này từng là nơi diễn ra các cuộc họp nội các quan trọng, các buổi lễ tiếp đón nguyên thủ quốc gia và các sự kiện ngoại giao lịch sử trước ngày 30 tháng 4 năm 1975.', 'phong-khanh-tiet-vi.mp3', 120),
(18, 5, 2, 'Welcome to the Chamber of Reception at the Independence Palace. This is the largest hall in the palace, designed with a capacity of over five hundred people. It historically served as the venue for crucial cabinet meetings and state receptions prior to 1975.', 'phong-khanh-tiet-en.mp3', 115),
(19, 5, 3, '欢迎光临独立宫庆节厅。这是整座宫殿中面积最大的厅堂，设计典雅庄重，可容纳五百余人。在1975年4月30日之前，这里曾是举行重要内阁会议、接待各国元首以及重大历史外交活动的重要场所。', 'phong-khanh-tiet-zh.mp3', 115),
(20, 5, 4, '統一会堂の慶節の間（カンティエット・ルーム）へようこそ。宮殿内で最も広い面積を誇るこの部屋は、500人以上を収容できる格式高い造りとなっています。1975年4月30日以前は、重要な閣僚会議や国家元首の歓迎式典、歴史的な外交行事の場として使われていました。', '5-ja.mp3', 120),
(45, 5, 5, '통일궁의 영빈관(경절당)에 오신 것을 환영합니다. 이곳은 궁전 내에서 가장 넓은 면적을 자랑하며 500명 이상을 수용할 수 있도록 격조 높게 설계되었습니다. 1975년 4월 30일 이전에는 주요 각료 회의, 국가 원수 접견식 및 역사적인 외교 행사가 열리던 장소였습니다.', '5-ko.mp3', 115),
(46, 5, 6, 'Добро пожаловать в Парадный приемный зал Дворца Независимости. Это самый просторный зал дворца, торжественно спроектированный для размещения более пятисот человек. До 30 апреля 1975 года здесь проходили важнейшие заседания кабинета министров, официальные приемы глав государств и исторические дипломатические события.', '5-ru.mp3', 115),
(47, 5, 7, 'Bienvenue dans la Salle de Réception solennelle du Palais de l''Indépendance. Plus vaste salle du palais, elle offre un cadre prestigieux pouvant accueillir plus de cinq cents invités. Avant le 30 avril 1975, s''y tenaient des réunions ministérielles décisives, des réceptions de chefs d''État et de grands événements diplomatiques.', '5-fr.mp3', 115),
(48, 5, 8, 'Benvenuti nella Sala dei Ricevimenti del Palazzo dell''Indipendenza. È la sala più grande dell''intero palazzo, progettata con solennità per accogliere oltre cinquecento persone. Prima del 30 aprile 1975, ha ospitato cruciali riunioni di gabinetto, cerimonie per capi di stato e storici eventi diplomatici.', '5-it.mp3', 115),
(49, 5, 9, 'Willkommen im Empfangssaal des Unabhängigkeitspalastes. Dies ist der größte Saal des Palastes, feierlich gestaltet und mit einer Kapazität für über fünfhundert Personen. Vor dem 30. April 1975 fanden hier wichtige Kabinettssitzungen, Staatsempfänge für Staatsoberhäupter und historische diplomatische Anlässe statt.', '5-de.mp3', 115),

-- Phòng Đại Yến (loc 6)
(21, 6, 1, 'Phòng Đại Yến tọa lạc tại tầng một của Dinh Độc Lập. Đây là nơi từng tổ chức các buổi chiêu đãi dạ tiệc cấp nhà nước với sức chứa hơn một trăm khách, nổi bật với phong cách kiến trúc kết hợp hài hòa giữa truyền thống phương Đông và hiện đại phương Tây.', 'phong-dai-yen-vi.mp3', 100),
(22, 6, 2, 'The Banquet Chamber is situated on the first floor of the Independence Palace. This hall previously hosted state banquets accommodating over one hundred distinguished guests, distinguished by an architectural design that harmoniously blends Eastern traditions with Western modern styles.', 'phong-dai-yen-en.mp3', 100),
(23, 6, 3, '大宴厅位于独立宫二楼。这里曾是举办容纳百余位贵宾的国家级盛大晚宴的场所，其建筑风格巧妙融合了东方传统韵味与西方现代美学。', 'phong-dai-yen-zh.mp3', 95),
(24, 6, 4, '大宴会場は統一会堂の2階に位置しています。ここは100名以上の賓客をもてなす国家的な晩餐会が開催された場所で、東洋の伝統と西洋の現代性が調和した素晴らしい建築美が特徴です。', '6-ja.mp3', 100),
(50, 6, 5, '대연회장은 통일궁 1층에 자리하고 있습니다. 100명이 넘는 귀빈을 모시는 국가 공식 만찬이 열리던 곳으로, 동양의 전통미와 서양의 현대적 건축 양식이 조화롭게 어우러진 것이 특징입니다.', '6-ko.mp3', 100),
(51, 6, 6, 'Большой банкетный зал расположен на втором этаже Дворца Независимости. Здесь устраивались государственные банкеты с участием более ста почетных гостей; интерьер примечателен гармоничным слиянием восточных традиций и западного модерна.', '6-ru.mp3', 100),
(52, 6, 7, 'La Salle des Banquets se trouve au premier étage du Palais de l''Indépendance. Elle accueillait les grands banquets d''État réunissant plus d''une centaine de convives d''honneur, et se distingue par une architecture alliant harmonieusement traditions orientales et modernité occidentale.', '6-fr.mp3', 100),
(53, 6, 8, 'La Sala dei Banchetti è situata al primo piano del Palazzo dell''Indipendenza. Questo salone ha ospitato sontuosi banchetti di Stato per oltre cento ospiti illustri, distinguendosi per uno stile architettonico che unisce armoniosamente la tradizione orientale alla modernità occidentale.', '6-it.mp3', 100),
(54, 6, 9, 'Der Festsaal befindet sich im ersten Stock des Unabhängigkeitspalastes. Hier wurden staatliche Bankette für über hundert geladene Gäste ausgerichtet. Der Saal besticht durch einen architektonischen Stil, der fernöstliche Traditionen harmonisch mit westlicher Moderne verbindet.', '6-de.mp3', 100)
ON DUPLICATE KEY UPDATE text_content=VALUES(text_content), audio_url=VALUES(audio_url), duration_in_seconds=VALUES(duration_in_seconds);

-- 6. Sample Playback Logs (Nhật ký nghe mẫu)
INSERT INTO playback_logs (id, location_id, language_code, listen_duration_seconds, trigger_type, latitude, longitude, device_id, created_at) VALUES
(1, 1, 'vi', 85, 'QR_CODE', 10.7602, 106.7031, 'demo-device-sgu', NOW() - INTERVAL 1 HOUR),
(2, 1, 'en', 80, 'QR_CODE', 10.7602, 106.7031, 'demo-device-sgu', NOW() - INTERVAL 3 HOUR),
(3, 3, 'vi', 90, 'GEOFENCE', 10.7631, 106.7075, 'demo-device-sgu', NOW() - INTERVAL 5 HOUR),
(4, 3, 'vi', 95, 'QR_CODE', 10.7631, 106.7075, 'demo-device-sgu', NOW() - INTERVAL 7 HOUR),
(5, 5, 'vi', 110, 'QR_CODE', 10.7771, 106.6954, 'demo-device-sgu', NOW() - INTERVAL 10 HOUR),
(6, 5, 'en', 115, 'QR_CODE', 10.7771, 106.6954, 'demo-device-sgu', NOW() - INTERVAL 12 HOUR),
(7, 2, 'vi', 75, 'GEOFENCE', 10.7585, 106.7012, 'demo-device-sgu', NOW() - INTERVAL 18 HOUR)
ON DUPLICATE KEY UPDATE listen_duration_seconds=VALUES(listen_duration_seconds);
