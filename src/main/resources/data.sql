insert into product_type (type_name, create_date, last_modified_date)
values ('게임$타이틀', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('게임$게임기', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('게임$주변기기', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('도서$IT', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('도서$문제지', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('도서$소설', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('도서$유아', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('식료품$유제품', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('식료품$밀키트', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');
insert into product_type (type_name, create_date, last_modified_date)
values ('식표품$과일', '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');

insert into member (login_type, social_id, nick_name, email, profile_image_url, profile_image_down_load_url,
                    role, is_ban, create_date, last_modified_date)
values ('NAVER', 'fewklqrmkclvmkjwojasfklwefqwcxz12312', 'Test123', 'test@test.naver.com',
        'profileImg/1', 'https://downloadUrl/profileImg/1', 'ROLE_MEMBER', false,
        '2024-07-29 22:35:35.746859', '2024-07-29 22:35:35.746859');

insert into product (seller_id, product_type_id, name, price, discount_amount,  discount_rate,
                      is_ban, score_avg, create_date, last_modified_date)
values (1, 1, '젤다의 전설', 50000, 2000, 10.0, false, 0,
        '2024-07-29 23:35:35.746859', '2024-07-29 23:35:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (1, '{"index":1,"blockType":"IMAGE_TYPE","imageUri":"blockImage/2/e5e55e9e-83c8-4c34-8332-b10a9ad038c2.png","downloadUrl":"http://localhost:4566/test-bucket/blockImage/2/e5e55e9e-83c8-4c34-8332-b10a9ad038c2.png"}',
        'IMAGE_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (1, '{"index":2,"blockType":"TEXT_TYPE","content":"text 내용입니다."}',
        'TEXT_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (1, '{"index":3,"blockType":"IMAGE_TYPE","imageUri":"blockImage/2/05b244c3-0756-4be4-b5c8-7d84b1e827af.png","downloadUrl":"http://localhost:4566/test-bucket/blockImage/2/05b244c3-0756-4be4-b5c8-7d84b1e827af.png"}',
        'IMAGE_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_image (product_id,  image_uri, down_load_url, create_date, last_modified_date)
values (1, 'productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png', 'http://localhost:4566/test-bucket/productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png',
        '2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_image (product_id,  image_uri, down_load_url, create_date, last_modified_date)
values (1, 'productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png', 'http://localhost:4566/test-bucket/productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png',
        '2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '디럭스에디션1', 30000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '디럭스에디션2', 32000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '디럭스에디션3', 33000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '포스터', 1000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '굿즈인형', 5000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (1, '아트북', 20000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');

insert into product (seller_id, product_type_id, name, price, discount_amount,  discount_rate,
                     is_ban, score_avg, create_date, last_modified_date)
values (1, 1, '마리오', 50000, 2000, 10.0, false, 0,
        '2024-07-29 23:35:35.746859', '2024-07-29 23:35:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (2, '{"index":1,"blockType":"IMAGE_TYPE","imageUri":"blockImage/2/e5e55e9e-83c8-4c34-8332-b10a9ad038c2.png","downloadUrl":"http://localhost:4566/test-bucket/blockImage/2/e5e55e9e-83c8-4c34-8332-b10a9ad038c2.png"}',
        'IMAGE_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (2, '{"index":2,"blockType":"TEXT_TYPE","content":"text 내용입니다."}',
        'TEXT_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_content (product_id, content, type, create_date, last_modified_date)
values (2, '{"index":3,"blockType":"IMAGE_TYPE","imageUri":"blockImage/2/05b244c3-0756-4be4-b5c8-7d84b1e827af.png","downloadUrl":"http://localhost:4566/test-bucket/blockImage/2/05b244c3-0756-4be4-b5c8-7d84b1e827af.png"}',
        'IMAGE_TYPE', '2024-07-29 23:36:35.746859', '2024-07-29 23:36:35.746859');
insert into product_image (product_id,  image_uri, down_load_url, create_date, last_modified_date)
values (2, 'productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png', 'http://localhost:4566/test-bucket/productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png',
        '2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_image (product_id,  image_uri, down_load_url, create_date, last_modified_date)
values (2, 'productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png', 'http://localhost:4566/test-bucket/productImage/1/45409897-5eab-4468-8c5e-f2371e4ba2de.png',
        '2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '디럭스에디션1', 30000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '디럭스에디션2', 32000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_single_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '디럭스에디션3', 33000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '포스터', 1000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '굿즈인형', 5000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');
insert into product_multiple_option (product_id, option_name, price_change_amount, create_date, last_modified_date)
values (2, '아트북', 20000,'2024-07-29 23:37:35.746859', '2024-07-29 23:37:35.746859');