#! /bin/bash

function printHelpAndExit {
	echo "usage: project-generator.sh templateDir targetDir patternStr1 replaceStr1 [patternStr2 replaceStr2] ..."
	exit 1;
}

ARGS=("$@");
SCRIPT_PATH="`dirname \"$0\"`"

#get template and target dir
TEMPLATE_DIR="${ARGS[0]}"
TARGET_DIR="${ARGS[1]}"
#remove those first two args
ARGS=("${ARGS[@]:2}")
#check template and target dir
if [[ $TEMPLATE_DIR"" == "" ]] ; then
	echo "templateDir is not defined";
	printHelpAndExit;
fi
if [[ $TARGET_DIR"" == "" ]] ; then
	echo "targetDir is not defined";
	printHelpAndExit;
fi
if [[ ! -d "$TEMPLATE_DIR" ]]; then
	echo "templateDir must exists and must be dir";
	printHelpAndExit;
fi
if [[ -e "$TARGET_DIR" ]]; then
	echo "targetDir must not exists";
	printHelpAndExit;
fi


mkdir -p "$TARGET_DIR";
if [[ ! -d "$TARGET_DIR" ]]; then
	echo "could not create targetDir";
	exit 1;
fi
#rmdir "$TARGET_DIR";


#copy template into target
cp -Lrp "$TEMPLATE_DIR"/* "$TARGET_DIR";
#cp -Lrp "$TEMPLATE_DIR" "$TARGET_DIR";

#run renaming in targetDirectory
"$SCRIPT_PATH/rename.groovy" "$TARGET_DIR" "${ARGS[@]}"
retval=$?
if [[ $retval -ne 0 ]] ; then
	exit $retval
fi

#replace in files
while [ ${#ARGS[@]} -gt 0 ] ; do
	PATTERN="${ARGS[0]}"
	REPLACE="${ARGS[1]}"
	ARGS=("${ARGS[@]:2}")
	find "$TARGET_DIR" -type f -exec sed -i -s "s/${PATTERN}/${REPLACE}/g" {}  \; 
done




