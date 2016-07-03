import sys

def main():
	with open(sys.argv[1],'r') as f:
		lines = f.readlines()
	bigrams = []
	total_count = 0;
	for line in lines:
		l = line.split('\t')
		bigrams.append((l[0], int(l[1])))
		total_count += int(l[1])
	bigrams.sort(reverse=True, key=lambda x: x[1])
	#print bigrams
	# is it # distinct bigrams or total count of bigrams met
	print 'Number of distinct bigrams: ', len(bigrams)
	print 'Total number of bigrams: ', total_count

	if len(bigrams) > 0:
		print 'Most common bigram: ', bigrams[0][0]
		need_bigrams = int(0.1 * total_count)
		current_sum = 0
		current_bigram = 0
		while current_sum < need_bigrams:
			current_sum += bigrams[current_bigram][1]
			current_bigram += 1
		print 'Number of bigrams required to add up to 10% of all bigrams: ', current_bigram


if __name__ == '__main__':
	main()
