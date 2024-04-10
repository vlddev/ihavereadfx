1. export goodreads data to CSV-file
2. export ihaveread data from sqlite-db to TSV

           SELECT bn.id,
                 (select group_concat(a.name, '; ') from author a, author_book ab where ab.book_id = b.id and ab.author_id = a.id) authors,
                 bn.name,
                bn.lang
           FROM book_names bn, book b
           WHERE
             bn.book_id = b.id
           order by 1

3. import goodreads CSV-file to postgres table (goodreads_library_export)
4. import sqlite-db TSV-file to postgres table (ihaveread_books)
5. Tables goodreads_library_export, ihaveread_books must have columns id, title, author
6. Create GIST-index on goodreads_library_export (pg_trgm extension must be installed)

        > CREATE INDEX goodreads_trgm_idx ON public.goodreads_library_export USING gist (title gist_trgm_ops);
7. Create table with matches from goodreads_library_export and ihaveread_books

       create table matched_books as
       select id, title, author, best_match[1] goodreads_id, best_match[2] m_title, best_match[3] m_author, best_match[4] m_score from (
       select id, title, author,
       (SELECT array[g.id, g.title, g.author, to_char(similarity(g.title, ib.title), '99.99')]
       from goodreads_library_export g
       where g.title % ib.title
       ORDER by similarity(g.title, ib.title) desc limit 1
       ) as best_match
       from ihaveread_books ib ) as tbl

       CONSTRAINT matched_books_pk PRIMARY KEY (id)
8. change type of matched_books.m_score to float8
9. create script to update sqlite-db with perfect matches

        select 'update book_names set goodreads_id = '|| goodreads_id || ' where id = '|| id || ';' from matched_books mb 
        where m_score = 1.0;
10. the rest of data in matched_books must be processed manually
     1. delete perfect matches: `delete from matched_books where m_score = 1.0`
     2. delete rows without matches: `delete from matched_books where m_score is null` 
     3. add column checked `ALTER TABLE public.matched_books ADD checked bool NULL;`
     4. check matches manually