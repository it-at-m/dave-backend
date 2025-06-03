delete from pkweinheit;

insert into pkweinheit (id,
                        created_time,
                        version,
                        busse,
                        fahrradfahrer,
                        kraftraeder,
                        lastzuege,
                        lkw,
                        pkw)
values
    (
     gen_random_uuid(),
     now(),
     0,
     3.50,
     0.30,
     0.50,
     4.00,
     2.00,
     1.00
    );