# House keeper
FROM iron/base

RUN apk update && apk add bash && apk add curl

ADD run.sh /

CMD [ "/run.sh" ]
