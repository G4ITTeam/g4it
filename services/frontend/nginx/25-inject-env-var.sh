#!/usr/bin/env sh
ME=$(basename $0)

# Default env vars
if ! printenv | grep -q "KEYCLOAK_ENABLED";then
  export KEYCLOAK_ENABLED="true"
fi

export EXISTING_VARS=$(printenv | awk -F= '{print $1}' | sed 's/^/\$/g' | paste -sd,);
FILES=$(ls $JSFOLDER/*.js $JSFOLDER/*.html);

echo "$ME: info: injecting $EXISTING_VARS"
for file in $FILES;
do
  cp $file /tmp/tmpfile
  envsubst $EXISTING_VARS < /tmp/tmpfile > $file
done

exit 0
