#!/usr/bin/python

from distutils.version import LooseVersion
import re

class FilterModule(object):
	def filters(self):
		return {
			'semver_match': self.semver_match
		}

	def semver_match(self, expression):
		split = re.split(r'\s+', expression)
		v1 = split[0].strip()
		expr = split[1].strip()
		v2 = split[2].strip()

		return match(compare(v1, v2), expr)

# compares to semantic version an returns an integer as a result
def compare(a, b):
	pattern = re.compile('-(.+$)')

	m1 = re.search(pattern, a)
	m2 = re.search(pattern, b)

	asuff = m1.group(1) if m1 else ''
	bsuff = m2.group(1) if m2 else ''

	aparts = re.sub(pattern, '', a).split('.')
	bparts = re.sub(pattern, '', b).split('.')

	for i in range(0, 3):
		aipart = int(aparts[i])
		bipart = int(bparts[i])

		if aipart > bipart:
			return 1

		if bipart > aipart:
			return -1

	if not asuff and bsuff:
		return 1

	if asuff and not bsuff:
		return -1

	return cmp(asuff, bsuff)

# matches a comparison result (1 - gt, 0 - eq, -1 - lt) to the provided expression (>, <, =, >=, <=)
def match(res, expr):
	if res == 1 and (expr == '>' or expr == '>='):
		return True

	if res == -1 and (expr == '<' or expr == '<='):
		return True

	if res == 0 and (expr == '>=' or expr == '>=' or expr == '='):
		return True

	return False
