# Development hints

## SQL Statements

SELECT sum(pkw), count(pkw), max(pkw), startuhrzeit::time FROM public.zeitintervall 
where startuhrzeit between '2026-03-05' and '2026-03-06'
group by startuhrzeit::time order by startuhrzeit::time ASC limit 300;

SELECT sum(pkw)/(DATE_PART('day', '2026-03-06'::timestamp - '2026-03-05'::timestamp) + 1), 
startuhrzeit::time
FROM public.zeitintervall 
where startuhrzeit between '2026-03-05' and '2026-03-07' group by startuhrzeit::time
order by startuhrzeit::time ASC;

SELECT pkw, startuhrzeit::time, startuhrzeit::date, 
DATE_PART('day', '2026-03-06'::timestamp - '2026-03-05'::timestamp)+1 FROM public.zeitintervall 
where startuhrzeit between '2026-03-05' and '2026-03-07'
order by startuhrzeit::time ASC;

select sum(sumpkw),count(startuhrzeit::time), startuhrzeit::time from (
select sum(pkw) as sumpkw, startuhrzeit
FROM public.zeitintervall 
where startuhrzeit between '2026-03-05' and '2026-03-12' and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5)
and zaehlung_id = '339f992e-0925-4f6d-9e75-099bc520ad2c' group by startuhrzeit)
group by startuhrzeit::time order by startuhrzeit::time ASC;

select sum(sumpkw),count(startuhrzeit::time), startuhrzeit::time from (
select sum(pkw) as sumpkw, startuhrzeit
FROM public.zeitintervall 
where startuhrzeit between '2026-03-05' and '2026-03-12' and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5) and type = 'STUNDE_VIERTEL'
and zaehlung_id = '339f992e-0925-4f6d-9e75-099bc520ad2c' group by startuhrzeit)
group by startuhrzeit::time order by startuhrzeit::time ASC;

select 
	round(sum(pkw)/count(startuhrzeit::time)) as pkw, 
	round(sum(lkw)/count(startuhrzeit::time)) as lkw,
	round(sum (lastzuege)/count(startuhrzeit::time)) as lastzuege,
	round(sum(busse)/count(startuhrzeit::time)) as busse,
	round(sum(kraftraeder)/count(startuhrzeit::time)) as kraftraeder,
	round(sum(fahrradfahrer)/count(startuhrzeit::time)) as fahrradfahrer,
	round(sum(fussgaenger)/count(startuhrzeit::time)) as fussgaenger,
	startuhrzeit::time, 
	endeuhrzeit::time 
from (
select 
	sum(pkw) as pkw, 
	sum(lkw) as lkw,
	sum (lastzuege) as lastzuege,
	sum(busse) as busse,
	sum(kraftraeder) as kraftraeder,
	sum(fahrradfahrer) as fahrradfahrer,
	sum(fussgaenger) as fussgaenger,
	startuhrzeit, 
	endeuhrzeit
FROM public.zeitintervall 
where startuhrzeit between '2026-03-05 00:00:00' and '2026-03-06 23:59:59' and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5) and type = 'STUNDE_VIERTEL'
and zaehlung_id = '339f992e-0925-4f6d-9e75-099bc520ad2c' group by startuhrzeit, endeuhrzeit)
group by startuhrzeit::time, endeuhrzeit::time order by startuhrzeit::time ASC

select 
	zaehlung_id,
	round(sum(pkw)/count(startuhrzeit::time)) as pkw, 
	round(sum(lkw)/count(startuhrzeit::time)) as lkw,
	round(sum (lastzuege)/count(startuhrzeit::time)) as lastzuege,
	round(sum(busse)/count(startuhrzeit::time)) as busse,
	round(sum(kraftraeder)/count(startuhrzeit::time)) as kraftraeder,
	round(sum(fahrradfahrer)/count(startuhrzeit::time)) as fahrradfahrer,
	round(sum(fussgaenger)/count(startuhrzeit::time)) as fussgaenger,
	round(sum(hochrechnung_hochrechnungkfz)/count(startuhrzeit::time),2) as hochrechnungkfz,
	round(sum(hochrechnung_hochrechnunggv)/count(startuhrzeit::time),2) as hochrechnunggv,
	round(sum(hochrechnung_hochrechnungsv)/count(startuhrzeit::time),2) as hochrechnungsv,
	round(sum(hochrechnungrad)/count(startuhrzeit::time)) as hochrechnungrad,
	(CURRENT_DATE + startuhrzeit::time) as startUhrzeit, 
	(CURRENT_DATE + endeuhrzeit::time) as endeUhrzeit 
from (
select 
	zaehlung_id,
	sum(pkw) as pkw, 
	sum(lkw) as lkw,
	sum (lastzuege) as lastzuege,
	sum(busse) as busse,
	sum(kraftraeder) as kraftraeder,
	sum(fahrradfahrer) as fahrradfahrer,
	sum(fussgaenger) as fussgaenger,
	sum(hochrechnung_hochrechnungkfz) as hochrechnung_hochrechnungkfz,
	sum(hochrechnung_hochrechnunggv) as hochrechnung_hochrechnunggv,
	sum(hochrechnung_hochrechnungsv) as hochrechnung_hochrechnungsv,
	sum(hochrechnungrad) as hochrechnungrad,
	startuhrzeit, 
	endeuhrzeit
FROM public.zeitintervall 
where startuhrzeit between '2026-03-05 00:00:00' and '2026-03-06 23:59:59' and EXTRACT(DOW FROM startuhrzeit) IN (1, 2, 3, 4, 5) and type = 'STUNDE_VIERTEL'
and zaehlung_id = '339f992e-0925-4f6d-9e75-099bc520ad2c' group by startuhrzeit, endeuhrzeit, zaehlung_id)
group by startuhrzeit::time, endeuhrzeit::time, zaehlung_id order by startUhrzeit ASC