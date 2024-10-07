#!/usr/bin/env sh
ME=$(basename $0)

export EXISTING_VARS=$(printenv | awk -F= '{print $1}' | sed 's/^/\$/g' | paste -sd,);
FILES=$(ls /usr/share/nginx/html/*.js /usr/share/nginx/html/*.html);

echo "$ME: info: injecting $EXISTING_VARS"
for file in $FILES;
do
  cp $file /tmp/tmpfile
  envsubst $EXISTING_VARS < /tmp/tmpfile > $file
done

exit 0
