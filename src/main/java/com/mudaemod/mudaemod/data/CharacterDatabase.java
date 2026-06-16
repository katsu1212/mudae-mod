package com.mudaemod.mudaemod.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Curated list of popular anime characters with verified Minecraft skin UUIDs.
 * UUIDs were looked up via Mojang API for players named after these characters.
 * Only characters whose MC usernames actually exist are included.
 */
public class CharacterDatabase {

    public record Entry(int id, String name, String animeName, int kakeraValue, boolean waifu, String skinUUID) {}

    private static final Random RANDOM = new Random();

    // ── WAIFUS ──────────────────────────────────────────────────────────────────
    private static final List<Entry> WAIFUS = List.of(
        new Entry(1,   "Rem",        "Re:Zero",                          2000, true, "9ad71b71-6c7f-47bd-b6f3-e975770f212c"),
        new Entry(2,   "Asuna",      "Sword Art Online",                 1500, true, "00ca8f7b-ed67-4e24-9d69-1a206f1c1823"),
        new Entry(3,   "Emilia",     "Re:Zero",                          1200, true, "7a9a6cea-aea3-45e8-851b-e494524c2466"),
        new Entry(4,   "Ram",        "Re:Zero",                           800, true, "4adc9ee2-ed57-41a7-9441-80b26c542464"),
        new Entry(5,   "Mikasa",     "Attack on Titan",                  1800, true, "291ad9cf-693d-486d-9013-f72ef2a1f7e9"),
        new Entry(6,   "Nezuko",     "Demon Slayer",                     1600, true, "8a92c641-6c12-410c-9953-c201f77a88ff"),
        new Entry(7,   "Hinata",     "Naruto",                           1000, true, "a5eb8dd2-69a7-49e1-ad4e-521c50e1a2a4"),
        new Entry(8,   "Nami",       "One Piece",                         800, true, "d253df1a-0069-4c79-bfae-6454fd9e0bce"),
        new Entry(9,   "Robin",      "One Piece",                         900, true, "8c5f0eff-ddb3-413f-9bf2-0fb7843b2cce"),
        new Entry(10,  "Aqua",       "KonoSuba",                          700, true, "1b9aceed-2c6a-431c-b025-dfb90ea880ca"),
        new Entry(11,  "Megumin",    "KonoSuba",                         1400, true, "9ef0758a-1f50-4de3-acf0-227bfaf15ea3"),
        new Entry(12,  "Darkness",   "KonoSuba",                          600, true, "ae912f52-5c42-4823-ae54-f81f07277f6d"),
        new Entry(13,  "Wiz",        "KonoSuba",                          500, true, "a98464ff-a83e-478d-afaa-c41a54f3a703"),
        new Entry(14,  "Erza",       "Fairy Tail",                        900, true, "823915a0-9446-4745-b10c-5dba75ea187a"),
        new Entry(15,  "Lucy",       "Fairy Tail",                        700, true, "d543d5f4-5a64-4317-ba1d-ae2240156ef4"),
        new Entry(16,  "Winry",      "Fullmetal Alchemist",               600, true, "2ab40856-e3ba-4db0-bb89-9048dc16d2c0"),
        new Entry(17,  "Rukia",      "Bleach",                            800, true, "8a573338-5bb3-4571-898b-f83fb12967e7"),
        new Entry(18,  "Tatsumaki",  "One Punch Man",                    1000, true, "7f7d4bd9-a3b7-4a80-9562-70987d718d1e"),
        new Entry(19,  "Madoka",     "Puella Magi Madoka Magica",         800, true, "578ddc8a-745c-4c1a-8e91-fc5a7aae285a"),
        new Entry(20,  "Homura",     "Puella Magi Madoka Magica",        1200, true, "dba5e4fe-b6d2-42f7-9067-5ca6931cb3db"),
        new Entry(21,  "Mami",       "Puella Magi Madoka Magica",         700, true, "44072e9e-77c1-4afd-9c7d-39d5cb143fa0"),
        new Entry(22,  "Sayaka",     "Puella Magi Madoka Magica",         600, true, "5239d473-2722-4181-8551-4f80e804b1ce"),
        new Entry(23,  "Kyoko",      "Puella Magi Madoka Magica",         700, true, "f332713e-709f-45ff-9bf0-f0c20884a446"),
        new Entry(24,  "Taiga",      "Toradora",                         1100, true, "07c95c7c-2570-4326-93dc-47d029c69fb7"),
        new Entry(25,  "Kurisu",     "Steins;Gate",                      1500, true, "d4bb0bb0-a988-4591-9567-0023f46a89bd"),
        new Entry(26,  "Haruhi",     "The Melancholy of Haruhi Suzumiya", 900, true, "841e2dfa-ca49-489c-8cf3-b0b726b195fe"),
        new Entry(27,  "Violet",     "Violet Evergarden",                1200, true, "809b980d-269a-4072-8d04-ba9bdaaeab05"),
        new Entry(28,  "Anya",       "Spy x Family",                     1800, true, "944a5891-ca8c-411f-b2c8-8befbda2ebd4"),
        new Entry(29,  "Yor",        "Spy x Family",                     1000, true, "e3ccac2e-ad37-43e6-86d3-7df133dda708"),
        new Entry(30,  "Sinon",      "Sword Art Online",                  700, true, "ae4207dd-7a93-4bed-8061-27cf450433b9"),
        new Entry(31,  "Raphtalia",  "The Rising of the Shield Hero",    1200, true, "31a95c5d-9513-4f73-b647-bd709a6e8cfb"),
        new Entry(32,  "Filo",       "The Rising of the Shield Hero",     500, true, "fb26064a-0400-4b72-9e2c-d6181600d603"),
        new Entry(33,  "Mitsuri",    "Demon Slayer",                      700, true, "2a3d3edb-63e0-498d-891a-c0f0072647e9"),
        new Entry(34,  "Kanao",      "Demon Slayer",                      600, true, "54f0a62a-3c1f-4f67-b60f-684701a38b83"),
        new Entry(35,  "Shinobu",    "Demon Slayer",                      800, true, "5c91163e-fce4-481c-a177-aeba8fd82c16"),
        new Entry(36,  "Uraraka",    "My Hero Academia",                  900, true, "80299fdb-51b1-4d4b-9269-2d1ee5ad1b04"),
        new Entry(37,  "Power",      "Chainsaw Man",                     1400, true, "370e31b1-43a2-4da1-bfa5-9b011f3e12c4"),
        new Entry(38,  "Makima",     "Chainsaw Man",                     1300, true, "80159d75-449a-4813-aa74-717bcf3b9fea"),
        new Entry(39,  "Mio",        "K-On!",                             800, true, "43f79582-18a2-4710-883f-f3ac68b5ac72"),
        new Entry(40,  "Rikka",      "Chuunibyou",                        700, true, "23552906-e1f3-49a8-893c-b42818434776"),
        new Entry(41,  "Konata",     "Lucky Star",                        600, true, "1315f2a7-c674-4631-9e84-043e111f56c9"),
        new Entry(42,  "Hori",       "Horimiya",                          800, true, "cb6a8f43-1557-43e6-baed-7619499035ba"),
        new Entry(43,  "Kaori",      "Your Lie in April",                1000, true, "779afe26-ee4a-4bc2-8e6a-0329faa85c07"),
        new Entry(44,  "Asuka",      "Neon Genesis Evangelion",          1000, true, "64bbc554-6178-429d-a6a0-1035fc07a4b8"),
        new Entry(45,  "Saber",      "Fate/stay night",                  1200, true, "33420443-5364-4215-b168-b4142eaed4fe"),
        new Entry(46,  "Hestia",     "DanMachi",                          900, true, "65fc2540-b010-4841-9f8c-27d9bfe46ca8"),
        new Entry(47,  "Erina",      "Food Wars",                         700, true, "2e8929a6-07cf-4b1a-a5d4-f0b680d6464e"),
        new Entry(48,  "Noelle",     "Black Clover",                      600, true, "cd092927-d2e8-40dc-b2d4-a27ef9e495c0"),
        new Entry(49,  "Misaka",     "A Certain Scientific Railgun",     1100, true, "2371ed45-3ec7-49d3-9388-319ce5d589e3"),
        new Entry(50,  "Albedo",     "Overlord",                          900, true, "ef29acab-e7ff-4c7e-9b32-6a749353fd94"),
        new Entry(51,  "Touka",      "Tokyo Ghoul",                       700, true, "07448352-23af-4f76-950c-081774febba0"),
        new Entry(52,  "Rize",       "Tokyo Ghoul",                       700, true, "d6241427-0a8d-47af-98b9-9bffe0395949"),
        new Entry(53,  "Nagisa",     "Clannad",                           800, true, "9d2bf4c8-ad43-451a-978d-057d082c4c93"),
        new Entry(54,  "Nobara",     "Jujutsu Kaisen",                    800, true, "68b35fca-3224-4771-b92c-910fe0cf4b26"),
        new Entry(55,  "Kaguya",     "Kaguya-sama: Love is War",         1000, true, "a6fa4dde-3ad0-4b6e-bec9-c7b9dfb1e315"),
        new Entry(56,  "Chika",      "Kaguya-sama: Love is War",          700, true, "754ba114-cbda-4043-bab8-58ea7623ff43"),
        new Entry(57,  "Yumeko",     "Kakegurui",                         900, true, "184f83b6-d5c8-46d1-a9ec-6cd0645d834e"),
        new Entry(58,  "Kanna",      "Miss Kobayashi's Dragon Maid",      700, true, "2599765b-b5e3-48d2-97d7-0fbf6953c827"),
        new Entry(59,  "Tohru",      "Miss Kobayashi's Dragon Maid",      800, true, "5460cb64-5124-470a-9f10-9cdab95fbbe4"),
        new Entry(60,  "Lucoa",      "Miss Kobayashi's Dragon Maid",      800, true, "53542e0a-5f92-4944-ba5a-202d9cd7911c"),
        new Entry(61,  "Elma",       "Miss Kobayashi's Dragon Maid",      600, true, "6a7b18ec-7648-478b-977f-e0b7a66a9e28"),
        new Entry(62,  "Zero Two",   "Darling in the FranXX",            1800, true, "c437b0e5-7fb0-468f-aa5c-536d1b361eb3"),
        new Entry(63,  "Yoko",       "Gurren Lagann",                     700, true, "50b67eb7-f124-415a-a7e5-883d4629493e"),
        new Entry(64,  "Ryuko",      "Kill la Kill",                      900, true, "be27249a-49b9-4a5c-be34-1172b8da8d6d"),
        new Entry(65,  "Satsuki",    "Kill la Kill",                      800, true, "390046e5-44a7-4716-abeb-5c5fed19a680"),
        new Entry(66,  "Miku",       "Vocaloid",                         1000, true, "7a64b4f8-4995-4704-aeb8-2c9bf0629bef"),
        new Entry(67,  "Ochako",     "My Hero Academia",                  800, true, "8651cc11-9620-4d01-90ff-1cf3eafb0e03"),
        new Entry(68,  "Nejire",     "My Hero Academia",                  600, true, "826c10ec-e6cf-4943-9f5a-c0c7202c45ee"),
        new Entry(69,  "Momo",       "My Hero Academia",                  700, true, "599ec515-b661-4f70-a509-e50fb07fdc4a"),
        new Entry(70,  "Kyoka",      "My Hero Academia",                  600, true, "91a457a4-4869-4adc-946a-2bfd96518a5a"),
        new Entry(71,  "Marin",      "My Dress-Up Darling",              1000, true, "0c15128a-62f9-4054-8b3e-c785e1344c40"),
        new Entry(72,  "Ichika",     "Darling in the FranXX",             600, true, "53b52c73-12b7-472e-9113-7799eaf6fe52"),
        new Entry(73,  "Daki",       "Demon Slayer",                      700, true, "59d46542-718e-439f-b1f5-12afb37a6a52"),
        new Entry(74,  "Kokoro",     "Darling in the FranXX",             600, true, "1be8579c-2c80-47b2-b5bc-1e0b5c82ffde")
    );

    // ── HUSBANDOS ────────────────────────────────────────────────────────────────
    private static final List<Entry> HUSBANDOS = List.of(
        new Entry(101, "Kirito",     "Sword Art Online",                 1200, false, "a489deac-9d31-4b9b-ba35-db993f6eeb79"),
        new Entry(102, "Goku",       "Dragon Ball",                      2000, false, "3929e052-bad7-4b74-8177-4d5ba591fbd4"),
        new Entry(103, "Naruto",     "Naruto",                           1800, false, "0dc42fac-c582-4bd4-9167-9e0bc2c972ce"),
        new Entry(104, "Sasuke",     "Naruto",                           1600, false, "7e2f8c98-3168-4ea8-b9c4-10e54edc332d"),
        new Entry(105, "Levi",       "Attack on Titan",                  2000, false, "6c989f35-30aa-4583-bd54-e07ab6151940"),
        new Entry(106, "Eren",       "Attack on Titan",                  1400, false, "d3cce278-4eb9-4baa-ae90-37ad7f50c056"),
        new Entry(107, "Zenitsu",    "Demon Slayer",                      900, false, "fc194421-64f8-458a-b1d5-958482a9d7b1"),
        new Entry(108, "Tanjiro",    "Demon Slayer",                     1600, false, "4cacf72d-a878-4a4e-a124-72a7e60f3dea"),
        new Entry(109, "Itachi",     "Naruto",                           1800, false, "8871b364-7c66-432a-883b-1017b6475942"),
        new Entry(110, "Kakashi",    "Naruto",                           1400, false, "a5a260a2-9456-481e-9f4a-922cf8de6a3f"),
        new Entry(111, "Vegeta",     "Dragon Ball",                      1500, false, "aa269575-7d09-4891-9c29-75f638df0141"),
        new Entry(112, "Luffy",      "One Piece",                        2000, false, "12c4e186-5700-4237-a6b8-56c05f72aab1"),
        new Entry(113, "Zoro",       "One Piece",                        1800, false, "7efda859-0038-46b2-9c27-a37cc3776bd4"),
        new Entry(114, "Sanji",      "One Piece",                        1000, false, "1d403219-a4e8-4ce5-bf99-93124846db80"),
        new Entry(115, "Ace",        "One Piece",                        1200, false, "35facc3f-7426-4050-80b6-fb2b761477e8"),
        new Entry(116, "Chopper",    "One Piece",                         700, false, "ee440d2e-c190-4bc9-b67c-213df5cf4c42"),
        new Entry(117, "Subaru",     "Re:Zero",                           700, false, "c54ec2e8-65b1-47b2-bbaf-72ea7771f785"),
        new Entry(118, "Deku",       "My Hero Academia",                 1400, false, "07b29e12-f9e7-4541-b6ad-727e6fc68de2"),
        new Entry(119, "Bakugo",     "My Hero Academia",                 1600, false, "24abcf66-1e47-42f6-9f7b-c29f873f9f74"),
        new Entry(120, "Todoroki",   "My Hero Academia",                 1400, false, "5138e05c-3e1f-47fc-91cd-497ee4a69410"),
        new Entry(121, "Saitama",    "One Punch Man",                    1500, false, "7e1f7c13-a400-4c6c-a247-a1d97811f9d5"),
        new Entry(122, "Genos",      "One Punch Man",                     700, false, "6b186191-ba7c-42f5-bde7-a4223d16269f"),
        new Entry(123, "Ichigo",     "Bleach",                           1500, false, "12a27ebd-58e3-4ac7-ba01-d09c5e3c2fc6"),
        new Entry(124, "Natsu",      "Fairy Tail",                       1000, false, "033eff79-b4ec-41f9-8167-1a27f74bc149"),
        new Entry(125, "Gray",       "Fairy Tail",                        700, false, "1326de59-987d-4b5d-a40d-75ff6dac16da"),
        new Entry(126, "Edward",     "Fullmetal Alchemist",              1200, false, "f404368f-c297-496e-8844-a1d0b47b21b5"),
        new Entry(127, "Gohan",      "Dragon Ball",                       900, false, "488fe73a-124b-4b0e-9007-1d52db676dab"),
        new Entry(128, "Lelouch",    "Code Geass",                       1500, false, "9a85455d-e11e-47a7-8010-b08c1c798cc1"),
        new Entry(129, "Suzaku",     "Code Geass",                        800, false, "48285d2b-45c0-46cb-9466-88ecc8ebeb3f"),
        new Entry(130, "Shinji",     "Neon Genesis Evangelion",           700, false, "b980de64-4c9b-43ab-999d-0fbe97156e56"),
        new Entry(131, "Gon",        "Hunter x Hunter",                  1000, false, "82bafdf8-7f04-4c6f-b382-c03e8790fccd"),
        new Entry(132, "Killua",     "Hunter x Hunter",                  1500, false, "eb45c43d-e14d-4236-bdc6-611f1fe2ef48"),
        new Entry(133, "Hisoka",     "Hunter x Hunter",                   900, false, "f26b26f0-106f-4b16-90d5-8a64c8c49539"),
        new Entry(134, "Giorno",     "JoJo's Bizarre Adventure",          900, false, "f10696bb-9866-40a6-ae64-4c8a49844f3d"),
        new Entry(135, "Jotaro",     "JoJo's Bizarre Adventure",         1000, false, "d13eca77-cc84-45fd-b063-c6682aabc733"),
        new Entry(136, "Dio",        "JoJo's Bizarre Adventure",         1000, false, "1d66328b-914f-44e4-a9cb-775c71db8ec8"),
        new Entry(137, "Josuke",     "JoJo's Bizarre Adventure",          700, false, "fbb25e8a-f49f-4c23-b86b-27783b6566ac"),
        new Entry(138, "Kira",       "JoJo's Bizarre Adventure",          800, false, "32215687-4dad-4506-bf4d-7c6f9951ec2e"),
        new Entry(139, "Narancia",   "JoJo's Bizarre Adventure",          600, false, "e0875365-18c6-48cd-aefd-ff6c0e90dd7f"),
        new Entry(140, "Fugo",       "JoJo's Bizarre Adventure",          600, false, "27bbd967-c836-408c-8ee7-286c4e5a8091"),
        new Entry(141, "Abbacchio",  "JoJo's Bizarre Adventure",          700, false, "44ff499f-a59e-44cf-be07-cf41c65e01bb"),
        new Entry(142, "Noriaki",    "JoJo's Bizarre Adventure",          600, false, "b860511c-f8db-487f-ba65-8ea6e05fae8d"),
        new Entry(143, "Touma",      "A Certain Magical Index",           700, false, "8f259f5b-0ac1-4318-9db5-df6483a58b71"),
        new Entry(144, "Kaneki",     "Tokyo Ghoul",                      1200, false, "dc42fad3-d9f1-4ea3-98e9-cb1658da51dc"),
        new Entry(145, "Sora",       "No Game No Life",                   800, false, "297275a7-4387-4633-acb8-bede106f2549"),
        new Entry(146, "Ainz",       "Overlord",                         1000, false, "cdb65b0e-c690-4df7-8515-14060eda1dd5"),
        new Entry(147, "Bell",       "DanMachi",                          800, false, "1a21eb8b-5084-4647-9539-b1f2cbcc9a65"),
        new Entry(148, "Okabe",      "Steins;Gate",                       900, false, "3c16565f-e7e9-4e0e-89a4-a8ea70c3a51d"),
        new Entry(149, "Soma",       "Food Wars",                         700, false, "2db109fe-1711-45e8-8eb2-72e64f4aed46"),
        new Entry(150, "Nagisa",     "Assassination Classroom",           800, false, "9d2bf4c8-ad43-451a-978d-057d082c4c93"),
        new Entry(151, "Asta",       "Black Clover",                      800, false, "9a86c0dc-0aa1-4e33-a2be-17218a0d9e00"),
        new Entry(152, "Yuno",       "Black Clover",                      700, false, "c1e7266f-af6c-416b-b6aa-d4b7268da353"),
        new Entry(153, "Inosuke",    "Demon Slayer",                      700, false, "6dbf4f61-c046-453f-a8a4-2668face482d"),
        new Entry(154, "Giyuu",      "Demon Slayer",                      900, false, "c2538f41-6ee2-456d-8721-00b320f2918e"),
        new Entry(155, "Rengoku",    "Demon Slayer",                     1000, false, "5f443d28-7319-42c3-95fe-6c3bafbd0ec9"),
        new Entry(156, "Gyomei",     "Demon Slayer",                      700, false, "a545d42d-3b57-45aa-a0da-2788afd2b868"),
        new Entry(157, "Tengen",     "Demon Slayer",                      700, false, "a043e5e2-6a47-47c2-acb6-36b31820443c"),
        new Entry(158, "Sanemi",     "Demon Slayer",                      700, false, "48769cb6-7423-4fba-89be-7049596dcee0"),
        new Entry(159, "Obanai",     "Demon Slayer",                      600, false, "c7879894-bc04-4f05-924e-d4fc315cd487"),
        new Entry(160, "Muzan",      "Demon Slayer",                      800, false, "4890d43f-1a65-4382-82f1-9cbbf240e246"),
        new Entry(161, "Gojo",       "Jujutsu Kaisen",                   1600, false, "8b6e6236-f651-43db-bf74-85f6ef459979"),
        new Entry(162, "Yuji",       "Jujutsu Kaisen",                   1200, false, "e99f00ec-4356-482d-ad97-b05e75d8f303"),
        new Entry(163, "Megumi",     "Jujutsu Kaisen",                    900, false, "4e2538c0-a462-4ed3-843d-ee059737c6a5"),
        new Entry(164, "Nanami",     "Jujutsu Kaisen",                    800, false, "89f22d7f-3755-4cdf-b0c0-29fea1a62fbe"),
        new Entry(165, "Toge",       "Jujutsu Kaisen",                    600, false, "27463aa7-4826-408e-a1ec-bd8b0201342b"),
        new Entry(166, "Miyamura",   "Horimiya",                          700, false, "c4d382bd-3e61-4f59-bc7c-e61e75346842"),
        new Entry(167, "Miyuki",     "Kaguya-sama: Love is War",          800, false, "eb71ad4d-cda1-4967-b5fa-34724c1de7ce"),
        new Entry(168, "Denji",      "Chainsaw Man",                     1000, false, "3a1cbc81-35d9-4fef-a11b-b50f8fc0e0e5"),
        new Entry(169, "Rimuru",     "That Time I Got Reincarnated as a Slime", 900, false, "518e7828-2297-4894-af21-2d88a5c012cf"),
        new Entry(170, "Guts",       "Berserk",                          1200, false, "5eb68b6a-b6e4-4376-a8c2-cada921590ca"),
        new Entry(171, "Spike",      "Cowboy Bebop",                     1000, false, "c0cc43a6-07a1-44ad-9a86-0b217ca9a9c3"),
        new Entry(172, "Kamina",     "Gurren Lagann",                     800, false, "494a35eb-b1f8-4698-871d-aa1d049f8eda"),
        new Entry(173, "Simon",      "Gurren Lagann",                     700, false, "f3c4dfb9-1c7b-40ac-81fd-462538538523"),
        new Entry(174, "Light",      "Death Note",                       1200, false, "00d313db-93d9-42fc-827d-a43aeafcad34"),
        new Entry(175, "Naofumi",    "The Rising of the Shield Hero",     700, false, "5f3176bd-8819-42bb-8550-b0ce29e204b6"),
        new Entry(176, "Denki",      "My Hero Academia",                  600, false, "9541eed0-db6b-49ff-900b-37fbf0603cb5"),
        new Entry(177, "Eijiro",     "My Hero Academia",                  600, false, "8494876d-1947-40a6-8bd1-10df737ff6fc"),
        new Entry(178, "Tamaki",     "My Hero Academia",                  700, false, "a086e718-befa-4f95-8bff-ac239a228426"),
        new Entry(179, "Mirio",      "My Hero Academia",                  700, false, "d035d484-c180-429c-8cae-c005fe87a71b"),
        new Entry(180, "Aizawa",     "My Hero Academia",                  800, false, "44ee7d74-ce87-48cd-b696-943311e39711"),
        new Entry(181, "Gyutaro",    "Demon Slayer",                      700, false, "8b1c2fd5-1f78-4bc0-b728-d1c0786aac1f"),
        new Entry(182, "Hiro",       "Darling in the FranXX",             700, false, "67d0cba2-74a3-4c77-97ab-5c9bce5928f3"),
        new Entry(183, "Kaworu",     "Neon Genesis Evangelion",           800, false, "20a94804-3ad2-4e8c-8187-5d67518af09c")
    );

    private static final List<Entry> ALL;
    static {
        ALL = new ArrayList<>();
        ALL.addAll(WAIFUS);
        ALL.addAll(HUSBANDOS);
    }

    public static Entry rollRandom(boolean waifu) {
        List<Entry> pool = waifu ? WAIFUS : HUSBANDOS;
        return pool.get(RANDOM.nextInt(pool.size()));
    }

    public static List<Entry> getAll() { return ALL; }
    public static List<Entry> getWaifus() { return WAIFUS; }
    public static List<Entry> getHusbandos() { return HUSBANDOS; }
}
