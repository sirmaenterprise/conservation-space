#!/usr/bin/env bats

DIR=${BATS_TEST_DIRNAME}

function replace() {
	awk -f ${DIR}/../rootfs/usr/local/bin/replace-env-vars.awk ${DIR}/$1 > ${DIR}/$2
}

@test "Replacing environment variables in an empty file" {
	replace resources/1-empty 1-result
	result=$(comm -23 ${DIR}/1-result ${DIR}/resources/1-empty | wc -l)
  	[ "$result" -eq 0 ]
}

@test "Replacing environment variables in a file without declared variables" {
	replace resources/2-nothing-to-replace 2-result
	result=$(comm -23 ${DIR}/2-result ${DIR}/resources/2-nothing-to-replace | wc -l)
  	[ "$result" -eq 0 ]
}

@test "Replacing environment variables in a file having some declared variables" {
	export SOMETHING="SoMeThiNG"
	export SECOND="SeconD"
	export END="EnD"
	export OR="Or"
	export QUOTES="quoteees"
	replace resources/3-proper 3-result
	result=$(comm -23 ${DIR}/3-result ${DIR}/resources/3-proper-expected | wc -l)
  	[ "$result" -eq 0 ]
}
